// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.redolog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.redolog.logger.FileLogReader;
import com.zimbra.cs.redolog.logger.LogWriter;
import com.zimbra.cs.redolog.op.AbortTxn;
import com.zimbra.cs.redolog.op.Checkpoint;
import com.zimbra.cs.redolog.op.CommitTxn;
import com.zimbra.cs.redolog.op.RedoableOp;
import com.zimbra.cs.redolog.op.StoreIncomingBlob;

/**
 * @since 2004. 7. 22.
 * @author jhahm
 */
public class RedoPlayer {

    private static final int INITIAL_MAP_SIZE = 1000;

    // Use a separate guard object to synchronize access to mOpsMap.
    // Don't synchronize on mOpsMap itself because it can get reassigned.
    private final Object mOpsMapGuard = new Object();

    // LinkedHashMap to ensure iteration order == insertion order
    private LinkedHashMap<TransactionId, RedoableOp> mOpsMap;

    private boolean mWritable;
    private boolean mUnloggedReplay;
    private boolean mIgnoreReplayErrors;
    private boolean mSkipDeleteOps;
    protected boolean handleMailboxConflict;
    protected ConcurrentMap<Integer, Integer> mailboxConflicts = new ConcurrentHashMap<Integer, Integer>();

    public RedoPlayer(boolean writable) {
        this(writable, false, false, false, false);
    }

    public RedoPlayer(boolean writable, boolean unloggedReplay, boolean ignoreReplayErrors, boolean skipDeleteOps, 
        boolean handleMailboxConflict) {
        mOpsMap = new LinkedHashMap<TransactionId, RedoableOp>(INITIAL_MAP_SIZE);
        mWritable = writable;
        mUnloggedReplay = unloggedReplay;
        mIgnoreReplayErrors = ignoreReplayErrors;
        mSkipDeleteOps = skipDeleteOps;
        this.handleMailboxConflict = handleMailboxConflict;
    }

    public void shutdown() {
        mOpsMap.clear();
    }

    public void scanLog(File logfile, boolean redoCommitted, Map<Integer, Integer> mboxIDsMap,
            long startTime, long endTime)
    throws IOException, ServiceException {
        scanLog(logfile, redoCommitted, mboxIDsMap, startTime, endTime, Long.MAX_VALUE);
    }

    /**
     * Scans a redo log file.  An op that is neither committed nor aborted is
     * added to mOpsMap.  These are the ops that need to be reattempted during
     * crash recovery.  If redoCommitted is true, an op is reattempted as soon
     * as its COMMIT entry is encountered.  This case is for replaying the logs
     * during mailbox restore.
     * @param logfile
     * @param redoCommitted
     * @param mboxIDsMap If not null, restrict replay of log entries to
     *                   mailboxes whose IDs are given by the key set of the
     *                   map.  Replay is done against mailboxes whose IDs are
     *                   given by the value set of the map.  Thus, it is
     *                   possible to replay operations from one mailbox in
     *                   a different mailbox.
     * @param startTime  Only process ops whose prepare time is at or later than
     *                   this time.
     * @param endTime    Only process ops whose commit time is before (but not
     *                   at) this time.
     * @param ignoreCommitsAtOrAfter Ops that were committed at or after this timestamp are ignored.
     *                               They will not be replayed even when redoCommitted=true.  They will
     *                               be considered uncommitted, and thus will become eligible for replay
     *                               during crash recovery.  For uses other than crash recovery, pass
     *                               Long.MAX_VALUE to not ignore any committed ops.
     * @throws IOException
     */
    private void scanLog(File logfile, boolean redoCommitted, Map<Integer, Integer> mboxIDsMap,
            long startTime, long endTime, long ignoreCommitsAtOrAfter)
    throws IOException, ServiceException {
        FileLogReader logReader = new FileLogReader(logfile, mWritable);
        logReader.open();
        long lastPosition = 0;

        // Read all ops in redo log, discarding those with commit/abort entries.
        try {
            RedoableOp op = null;
            while ((op = logReader.getNextOp()) != null) {
                lastPosition = logReader.position();

                // We can't break from the loop when op.getTimestamp() > endTime.  We could if ops in the file
                // were sorted by timestamp, but they are not.  Ops are executed and get their timestamps in
                // separate threads, then later are added to the log.  Logged order is not necessarily the same as
                // the order in which the threads looked at the clock.
                //
                // We have scan to the end of the file to know with certainty we've gone past the time limit.

                if (ZimbraLog.redolog.isDebugEnabled())
                    ZimbraLog.redolog.debug("Read: " + op);

                processOp(op, redoCommitted, mboxIDsMap, startTime, endTime, ignoreCommitsAtOrAfter);
            }
        } catch (IOException e) {
            // The IOException could be a real I/O problem or it could mean
            // there was a server crash previously and there were half-written
            // log entries.  We can't really tell which case it is, so just
            // assume the second case and truncate the file after the last
            // successfully read item.

            ZimbraLog.redolog.warn("IOException while reading redolog file", e);

            long size = logReader.getSize();
            if (lastPosition < size) {
                long diff = size - lastPosition;
                String msg =
                    "There were " + diff +
                    " bytes of junk data at the end of " +
                    logfile.getAbsolutePath() +
                    ".";
                if (mWritable) {
                    ZimbraLog.redolog.warn(msg + "  File will be truncated to " +
                            lastPosition + " bytes.");
                    logReader.truncate(lastPosition);
                } else
                    ZimbraLog.redolog.warn(msg);
            }
        } finally {
            logReader.close();
        }
    }

    // used to detect/track if a commit/abort record is played back
    // before its change record
    private boolean mHasOrphanOps = false;
    private Map<TransactionId, RedoableOp> mOrphanOps =
        new HashMap<TransactionId, RedoableOp>();

    private final void processOp(RedoableOp op,
            boolean redoCommitted,
            Map<Integer, Integer> mboxIDsMap,
            long startTime,
            long endTime,
            long ignoreCommitsAtOrAfter)
    throws ServiceException {

        if (op.isStartMarker()) {
            synchronized (mOpsMapGuard) {
                mOpsMap.put(op.getTransactionId(), op);
                if (mHasOrphanOps) {
                    RedoableOp x = mOrphanOps.remove(op.getTransactionId());
                    if (x != null)
                        ZimbraLog.redolog.error("Detected out-of-order insertion of change record for orphans commit/abort: change=" + op + ", orphan=" + x);
                }
            }
        } else {

            // When a checkpoint is encountered, discard all ops except
            // those listed in the checkpoint.
            if (op instanceof Checkpoint) {
                Checkpoint ckpt = (Checkpoint) op;
                Set txns = ckpt.getActiveTxns();
                if (txns.size() > 0) {
                    synchronized (mOpsMapGuard) {
                        if (mOpsMap.size() != txns.size()) {
                            // Unexpected discrepancy
                            if (ZimbraLog.redolog.isDebugEnabled()) {
                                StringBuffer sb1 = new StringBuffer("Current Uncommitted Ops: ");
                                StringBuffer sb2 = new StringBuffer("Checkpoint Uncommitted Ops: ");
                                int i = 0;
                                for (Iterator it = mOpsMap.keySet().iterator(); it.hasNext(); i++) {
                                    TransactionId id = (TransactionId) it.next();
                                    if (i > 0)
                                        sb1.append(", ");
                                    sb1.append(id);
                                }
                                i = 0;
                                for (Iterator it = txns.iterator(); it.hasNext(); i++) {
                                    TransactionId id = (TransactionId) it.next();
                                    if (i > 0)
                                        sb2.append(", ");
                                    sb2.append(id);
                                }
                                ZimbraLog.redolog.info("Checkpoint discrepancy: # current uncommitted ops = " + mOpsMap.size() +
                                        ", # checkpoint uncommitted ops = " + txns.size() +
                                        "\nMAP DUMP:\n" + sb1.toString() + "\n" + sb2.toString());
                            }
                        }
                    }
                } else {
                    synchronized (mOpsMapGuard) {
                        if (mOpsMap.size() != 0) {
                            // Unexpected discrepancy
                            if (ZimbraLog.redolog.isDebugEnabled()) {
                                StringBuffer sb1 = new StringBuffer("Current Uncommitted Ops: ");
                                int i = 0;
                                for (Iterator it = mOpsMap.keySet().iterator(); it.hasNext(); i++) {
                                    TransactionId id = (TransactionId) it.next();
                                    if (i > 0)
                                        sb1.append(", ");
                                    sb1.append(id);
                                }
                                ZimbraLog.redolog.info("Checkpoint discrepancy: # current uncommitted ops = " +
                                        mOpsMap.size() + " instead of 0\nMAP DUMP:\n" +
                                        sb1.toString());
                            }
                        }
                    }
                }
            } else if (op.isEndMarker()) {
                // Ignore if op is a commit and its timestamp is at or after ignoreCommitsAtOrAfter.
                // In other words, don't ignore if op is a rollback OR its timestamp is before ignoreCommitsAtOrAfter.
                boolean isCommitOp = op instanceof CommitTxn;
                long opTstamp = op.getTimestamp();
                if (!isCommitOp || opTstamp < ignoreCommitsAtOrAfter) {
                    // Encountered COMMIT or ABORT.  Discard the
                    // corresponding op from map, and optionally execute the committed op.
                    RedoableOp prepareOp;
                    synchronized (mOpsMapGuard) {
                        prepareOp = (RedoableOp) mOpsMap.remove(op.getTransactionId());
                        if (prepareOp == null) {
                            mHasOrphanOps = true;
                            ZimbraLog.redolog.error("Commit/abort record encountered before corresponding change record (" + op + ")");
                            TransactionId tid = op.getTransactionId();
                            RedoableOp x = (RedoableOp) mOrphanOps.get(tid);
                            if (x != null)
                                ZimbraLog.redolog.error("Op [" + op + "] is already in orphans map: value=" + x);
                            mOrphanOps.put(tid, op);
                        }
                    }

                    if (redoCommitted && prepareOp != null && isCommitOp &&
                            (startTime == -1 || prepareOp.getTimestamp() >= startTime) &&
                            opTstamp < endTime) {
                        boolean allowRedo = false;
                        if (mboxIDsMap == null) {
                            // Caller doesn't care which mailbox(es) the op is for.
                            allowRedo = true;
                        } else {
                            int opMailboxId = prepareOp.getMailboxId();
                            if (prepareOp instanceof StoreIncomingBlob) {
                                assert(opMailboxId == RedoableOp.MAILBOX_ID_ALL);
                                // special case for StoreIncomingBlob op that has
                                // a list of mailbox IDs.
                                StoreIncomingBlob storeOp = (StoreIncomingBlob) prepareOp;
                                List<Integer> list = storeOp.getMailboxIdList();
                                if (list != null) {
                                    Set<Integer> opMboxIds = new HashSet<Integer>(list);
                                    for (Map.Entry<Integer, Integer> entry : mboxIDsMap.entrySet()) {
                                        if (opMboxIds.contains(entry.getKey())) {
                                            allowRedo = true;
                                            // Replace the mailbox ID list in the op.  We're
                                            // replaying it only for the target mailbox ID we're
                                            // interested in.
                                            List<Integer> newList =
                                                new ArrayList<Integer>(mboxIDsMap.values());
                                            storeOp.setMailboxIdList(newList);
                                            break;
                                        }
                                    }
                                } else {
                                    // Prior to redolog version 1.0 StoreIncomingBlob
                                    // didn't keep track of mailbox list.  Always recreate
                                    // the blob since we don't know which mailboxes will
                                    // need it.
                                    allowRedo = true;
                                }
                            } else if (opMailboxId == RedoableOp.MAILBOX_ID_ALL) {
                                // This case should be checked after StoreIncomingBlob
                                // case because StoreIncomingBlob has mailbox ID of
                                // MAILBOX_ID_ALL.
                                allowRedo = true;
                            } else {
                                for (Map.Entry<Integer, Integer> entry : mboxIDsMap.entrySet()) {
                                    if (opMailboxId == entry.getKey().intValue()) {
                                        if (entry.getValue() != null) {
                                            // restore to a different mailbox
                                            prepareOp.setMailboxId(entry.getValue().intValue());
                                        }
                                        allowRedo = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (allowRedo) {
                            if (mSkipDeleteOps && prepareOp.isDeleteOp()) {
                                ZimbraLog.redolog.info("Skipping delete op: " + prepareOp.toString());
                            } else {
                                try {
                                    if (ZimbraLog.redolog.isDebugEnabled())
                                        ZimbraLog.redolog.debug("Redoing: " + prepareOp.toString());
                                    prepareOp.setUnloggedReplay(mUnloggedReplay);
                                    playOp(prepareOp);
                                } catch(Exception e) {
                                    if (!ignoreReplayErrors())
                                        throw ServiceException.FAILURE("Error executing redoOp", e);
                                    else
                                        ZimbraLog.redolog.warn(
                                                "Ignoring error during redo log replay: " + e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean ignoreReplayErrors() { return mIgnoreReplayErrors; }

    /**
     * Actually execute the operation.
     * @param op
     * @throws Exception
     */
    protected void playOp(RedoableOp op) throws Exception {
        if (handleMailboxConflict) {
            redoOpWithMboxConflict(op);
        } else {
            op.redo();
        }
    }
    
    protected void redoOpWithMboxConflict(RedoableOp op) throws Exception {
        try {
            Integer newId = mailboxConflicts.get(op.getMailboxId());
            
            if (newId != null) {
                ZimbraLog.redolog.warn("mailbox conflict, mapping old ID %d to %d", op.getMailboxId(), newId);
                op.setMailboxId(newId);
            }
            op.redo();
        } catch (MailboxIdConflictException mice) {
            ZimbraLog.redolog.warn("found mismatched mailboxId %d expected %d", mice.getFoundId(), mice.getExpectedId());
            mailboxConflicts.put(mice.getExpectedId(), mice.getFoundId());
        }
    }

    /**
     *
     * @param redoLogMgr
     * @param postStartupRecoveryOps operations to recover/redo after startup
     *                               completes and clients are allowed to
     *                               connect
     * @return number of operations redone (regardless of their success)
     * @throws Exception
     */
    public int runCrashRecovery(RedoLogManager redoLogMgr,
            List<RedoableOp> postStartupRecoveryOps)
    throws Exception {
        File redoLog = redoLogMgr.getLogFile();
        if (!redoLog.exists())
            return 0;

        long lookBackTstamp = Long.MAX_VALUE;
        long lookBackDuration = RedoConfig.redoLogCrashRecoveryLookbackSec() * 1000;
        if (lookBackDuration > 0) {
            // Guess the last op's timestamp.  Use the log file's last modified time.  Sanity check it by
            // going no earlier than the create time written in the log.  We can't rely on the last
            // op time field in the header because that is only accurate when the file was closed normally
            // but in crash recovery we're not dealing with a normally closed log.
            long logLastModTime = redoLog.lastModified();
            long logCreateTime = (new FileLogReader(redoLog)).getHeader().getCreateTime();
            long lastOpTstamp = Math.max(logLastModTime, logCreateTime);
            lookBackTstamp = lastOpTstamp - lookBackDuration;
        }

        // scanLog can truncate the current redo.log if it finds junk data at the end
        // from the previous crash.  Close log writer before scanning and reopen after
        // so we don't accidentally undo the truncation on the next write to the log.
        LogWriter logWriter = redoLogMgr.getLogWriter();
        logWriter.close();
        scanLog(redoLog, false, null, Long.MIN_VALUE, Long.MAX_VALUE, lookBackTstamp);
        logWriter.open();

        int numOps;
        synchronized (mOpsMapGuard) {
            numOps = mOpsMap.size();
        }
        if (numOps == 0) {
            ZimbraLog.redolog.info("No uncommitted transactions to redo");
            return 0;
        }

        synchronized (mOpsMapGuard) {
            Set entrySet = mOpsMap.entrySet();
            ZimbraLog.redolog.info("Redoing " + numOps + " uncommitted transactions");
            for (Iterator it = entrySet.iterator(); it.hasNext(); ) {
                Map.Entry entry = (Entry) it.next();
                RedoableOp op = (RedoableOp) entry.getValue();
                if (op == null)
                    continue;

                if (op.deferCrashRecovery()) {
                    ZimbraLog.redolog.info("Deferring crash recovery to after startup: " + op);
                    postStartupRecoveryOps.add(op);
                    continue;
                }

                if (ZimbraLog.redolog.isInfoEnabled())
                    ZimbraLog.redolog.info("REDOING: " + op);

                boolean success = false;
                try {
                    op.redo();
                    success = true;
                } catch (Exception e) {
                    ZimbraLog.redolog.error("Redo failed for [" + op + "]." +
                            "  Backend state of affected item is indeterminate." +
                            "  Marking operation as aborted and moving on.", e);
                } finally {
                    if (success) {
                        CommitTxn commit = new CommitTxn(op);
                        redoLogMgr.logOnly(commit, true);
                    } else {
                        AbortTxn abort = new AbortTxn(op);
                        redoLogMgr.logOnly(abort, true);
                    }
                }
            }
            mOpsMap.clear();
        }

        return numOps;
    }

    /**
     * Returns a copy of the pending ops map.
     * @return
     */
    protected LinkedHashMap<TransactionId, RedoableOp> getCopyOfUncommittedOpsMap() {
        LinkedHashMap<TransactionId, RedoableOp> map;
        synchronized (mOpsMapGuard) {
            if (mOpsMap != null)
                map = new LinkedHashMap<TransactionId, RedoableOp>(mOpsMap);
            else
                map = new LinkedHashMap<TransactionId, RedoableOp>();
        }
        return map;
    }
}

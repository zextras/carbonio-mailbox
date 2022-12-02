// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.zimbra.common.util.Constants;
import com.zimbra.common.util.MapUtil;
import com.zimbra.common.util.ZimbraLog;

public class ImapCommandThrottle {
    private ImapCommand lastCommand = null;
    private int repeats = 0;
    private int repeatLimit = 0;
    private static final long REPEAT_TIME_THRESHOLD = 5 * Constants.MILLIS_PER_MINUTE;
    private static final Map<String, ReentrantLock> commandLock = MapUtil.newTimeoutMap(1 * Constants.MILLIS_PER_HOUR);
    private static final long LOCK_TIMEOUT = 10 * Constants.MILLIS_PER_MINUTE;

    public ImapCommandThrottle(int repeatLimit) {
        super();
        this.repeatLimit = repeatLimit;
    }

    public int getRepeatLimit() {
        return repeatLimit;
    }

    private boolean isCommandRepeated(ImapCommand command) {
        return command != null && lastCommand != null
                && (command.getCreateTime() - lastCommand.getCreateTime() < REPEAT_TIME_THRESHOLD)
                && command.isDuplicate(lastCommand);
    }

    public boolean isCommandThrottled(ImapCommand command) {
        if (repeatLimit <= 0) {
            return false;
        } else if (command.throttle(lastCommand)) {
            // commands can implement their own throttle mechanism
            ZimbraLog.imap.debug("throttled by command");
            return true;
        } else if (isCommandRepeated(command)) {
            repeats++;
            lastCommand = command;
            if (repeats > repeatLimit) {
                ZimbraLog.imap.debug("throttled by repeat");
                return true;
            } else {
                return false;
            }
        } else {
            repeats = 1;
            lastCommand = command;
            return false;
        }
    }

    public void reset() {
        repeats = 0;
        lastCommand = null;
    }

    /**
     * Obtain a lock for serialization of expensive commands such as FETCH.
     * Each authenticated account may only perform one expensive command at a time regardless of number of sessions/connections
     * @param accountId - the authenticated account ID
     * @return ReentrantLock instance. Caller *must* unlock it when finished with the expensive operation
     * @throws ImapThrottledException
     */
    public ReentrantLock lock(String accountId) throws ImapThrottledException {
        ReentrantLock lock = null;
        synchronized (commandLock) {
            lock = commandLock.get(accountId);
            if (lock == null) {
                lock = new ReentrantLock();
                commandLock.put(accountId, lock);
            }
        }
        boolean locked = false;
        try {
            locked = lock.tryLock(LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        if (!locked) {
            throw new ImapThrottledException("Unable to obtain command lock " + lock.toString() + " aborting operation");
        } else {
            return lock;
        }
    }
}

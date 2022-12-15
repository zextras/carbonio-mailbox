// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 11. 15.
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

/**
 * THIS REDOLOG OPERATION IS DEPRECATED.  Backing up a mailbox will no longer
 * log this operation in redolog.
 *
 * This operation is a marker within a redo log file to help locate the
 * redo log file that corresponds to a particular backup.  When restoring
 * a mailbox from backup, the mailbox is reinitialized first, the data
 * from the most recent backup is restored, and finally all redos since
 * that backup should be replayed.  This marker helps us determine where
 * to start doing the redos.
 */
public class BackupMailbox extends RedoableOp {

    private long mBackupSetTstamp;  // timestamp of when backup set started (backup set = one or more mailboxes)
    private long mStartTime;        // timestamp of when backup of this mailbox started
    private long mEndTime;          // when backup of this mailbox finished (probably not that important)
    private String mLabel;          // any random label/description for this backup

    public BackupMailbox() {
        super(MailboxOperation.BackupMailbox);
    }

    public BackupMailbox(int mailboxId, long backupSetTstamp, long startTime, long endTime, String label) {
        this();
        setMailboxId(mailboxId);
        mBackupSetTstamp = backupSetTstamp;
        mStartTime = startTime;
        mEndTime = endTime;
        mLabel = label;
    }

    @Override public void redo() throws Exception {
        // Nothing to do.  This operation only serves as a marker within a
        // redo log file to find the correct starting log to replay after
        // restoring a particular backup.
    }

    @Override protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("backupSetTstamp=");
        sb.append(mBackupSetTstamp).append(", startTime=").append(mStartTime);
        sb.append(", endTime=").append(mEndTime);
        if (mLabel != null)
            sb.append("label=\"").append(mLabel).append("\"");
        return sb.toString();
    }

    @Override protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeLong(mBackupSetTstamp);
        out.writeLong(mStartTime);
        out.writeLong(mEndTime);
        out.writeUTF(mLabel != null ? mLabel : "");
    }

    @Override protected void deserializeData(RedoLogInput in) throws IOException {
        mBackupSetTstamp = in.readLong();
        mStartTime = in.readLong();
        mEndTime = in.readLong();
        mLabel = in.readUTF();
    }

}

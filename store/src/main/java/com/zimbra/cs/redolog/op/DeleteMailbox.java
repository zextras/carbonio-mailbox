// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2005. 4. 4.
 */
package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

public class DeleteMailbox extends RedoableOp {

    public DeleteMailbox() {
        super(MailboxOperation.DeleteMailbox);
    }

    public DeleteMailbox(int mailboxId) {
        this();
        setMailboxId(mailboxId);
    }

    /* (non-Javadoc)
     * @see com.zimbra.cs.redolog.op.RedoableOp#redo()
     */
    @Override public void redo() throws Exception {
    }

    /* (non-Javadoc)
     * @see com.zimbra.cs.redolog.op.RedoableOp#getPrintableData()
     */
    @Override protected String getPrintableData() {
        // no members to print
        return null;
    }

    /* (non-Javadoc)
     * @see com.zimbra.cs.redolog.op.RedoableOp#serializeData(java.io.RedoLogOutput)
     */
    @Override protected void serializeData(RedoLogOutput out) {
        // no members to serialize
    }

    /* (non-Javadoc)
     * @see com.zimbra.cs.redolog.op.RedoableOp#deserializeData(java.io.RedoLogInput)
     */
    @Override protected void deserializeData(RedoLogInput in) {
        // no members to deserialize
    }

    @Override public boolean isDeleteOp() {
        return true;
    }
}

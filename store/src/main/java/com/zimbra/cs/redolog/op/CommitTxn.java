// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 7. 22.
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.zimbra.cs.redolog.op;

import java.io.IOException;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoCommitCallback;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;

/**
 * @author jhahm
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CommitTxn extends ControlOp {

    private MailboxOperation mTxnOpCode;

    public CommitTxn() {
        super(MailboxOperation.CommitTxn);
	}

    public CommitTxn(RedoableOp changeEntry) {
    	super(MailboxOperation.CommitTxn, changeEntry.getTransactionId());
        setMailboxId(changeEntry.getMailboxId());
        mTxnOpCode = changeEntry.getOperation();
        mCommitCallback = changeEntry.getCommitCallback();
    }

    public MailboxOperation getTxnOpCode() {
        return mTxnOpCode;
    }

    protected String getPrintableData() {
        StringBuffer sb = new StringBuffer("txnType=");
        sb.append(mTxnOpCode.name());
        return sb.toString();
    }

    protected void serializeData(RedoLogOutput out) throws IOException {
        out.writeInt(mTxnOpCode.getCode());
    }

    protected void deserializeData(RedoLogInput in) throws IOException {
        mTxnOpCode = MailboxOperation.fromInt(in.readInt());
    }

    /**
     * Returns the callback object that was passed in at transaction start time.
     * @return
     */
    public RedoCommitCallback getCallback() {
        return mCommitCallback;
    }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 7. 23.
 */
package com.zimbra.cs.redolog.op;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import com.zimbra.cs.redolog.TransactionId;

/**
 * @author jhahm
 *
 * A Checkpoint record is written at the end of a redolog file as it is rolled
 * over.  It lists all uncommitted (i.e. in-progress) operations at the time
 * of log rollover.  Currently checkpoint information is not used.  One
 * potential use is logfile validity check.
 */
public class Checkpoint extends ControlOp {

	LinkedHashSet<TransactionId> mTxnSet;

	public Checkpoint() {
	    super(MailboxOperation.Checkpoint);
		mTxnSet = new LinkedHashSet<TransactionId>();
	}

	public Checkpoint(LinkedHashSet<TransactionId> txns) {
        super(MailboxOperation.Checkpoint);
		mTxnSet = txns;
		setTransactionId(new TransactionId());  // don't need a real txnid for checkpoint record
	}

	public int getNumActiveTxns() {
		return mTxnSet.size();
	}

	public Set<TransactionId> getActiveTxns() {
		return mTxnSet;
	}

	public void log() {
		assert false : "this method should not be called";
	}

	protected String getPrintableData() {
		if (mTxnSet.size() > 0) {
			StringBuffer sb = new StringBuffer();
            sb.append(mTxnSet.size()).append(" active txns: ");
			int i = 0;
			for (Iterator it = mTxnSet.iterator(); it.hasNext(); i++) {
				TransactionId txn = (TransactionId) it.next();
				if (i > 0)
					sb.append(", ");
				sb.append(txn.toString());
			}
			return sb.toString();
		} else
			return null;
	}

	protected void serializeData(RedoLogOutput out) throws IOException {
		out.writeInt(mTxnSet.size());
		for (Iterator it = mTxnSet.iterator(); it.hasNext(); ) {
			TransactionId txn = (TransactionId) it.next();
			txn.serialize(out);
		}
	}

	protected void deserializeData(RedoLogInput in) throws IOException {
		int num = in.readInt();
		for (int i = 0; i < num; i++) {
			TransactionId txn = new TransactionId();
			txn.deserialize(in);
			mTxnSet.add(txn);
		}
	}
}

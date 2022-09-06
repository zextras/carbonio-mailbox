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

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

/**
 * @author jhahm
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Generation - Code and Comments
 */
public class AbortTxn extends ControlOp {

  MailboxOperation mTxnOpCode;

  public AbortTxn() {
    super(MailboxOperation.AbortTxn);
  }

  public AbortTxn(RedoableOp changeEntry) {
    super(MailboxOperation.AbortTxn, changeEntry.getTransactionId());
    setMailboxId(changeEntry.getMailboxId());
    mTxnOpCode = changeEntry.getOperation();
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
}

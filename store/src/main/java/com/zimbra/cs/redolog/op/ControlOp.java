// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 7. 23.
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.zimbra.cs.redolog.op;

import com.zimbra.cs.mailbox.MailboxOperation;
import com.zimbra.cs.redolog.TransactionId;

/**
 * @author jhahm
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Generation - Code and Comments
 */
public abstract class ControlOp extends RedoableOp {

  public ControlOp(MailboxOperation op) {
    super(op);
  }

  public ControlOp(MailboxOperation op, TransactionId txnId) {
    this(op);
    setTransactionId(txnId);
    setTimestamp(System.currentTimeMillis());
  }

  public void redo() throws Exception {
    // do nothing
  }

  public void commit() {
    // do nothing
  }

  public void abort() {
    // do nothing
  }
}

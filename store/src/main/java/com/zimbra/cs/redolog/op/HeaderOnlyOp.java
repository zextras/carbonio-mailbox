// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on 2004. 11. 3.
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.zimbra.cs.redolog.op;

import com.zimbra.cs.redolog.RedoLogInput;
import com.zimbra.cs.redolog.RedoLogOutput;
import java.io.IOException;

/**
 * @author jhahm
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Generation - Code and Comments
 */
public class HeaderOnlyOp extends RedoableOp {

  private int mOpCode;
  private static final String sPrintable = "(detail skipped)";

  public HeaderOnlyOp(int code) {
    super(null);
    mOpCode = code;
  }

  /* (non-Javadoc)
   * @see com.zimbra.cs.redolog.op.RedoableOp#getOperationCode()
   */
  public int getOpCode() {
    return mOpCode;
  }

  /* (non-Javadoc)
   * @see com.zimbra.cs.redolog.op.RedoableOp#redo()
   */
  public void redo() throws Exception {
    // nothing to do
  }

  /* (non-Javadoc)
   * @see com.zimbra.cs.redolog.op.RedoableOp#getPrintableData()
   */
  protected String getPrintableData() {
    return sPrintable;
  }

  /* (non-Javadoc)
   * @see com.zimbra.cs.redolog.op.RedoableOp#serializeData(java.io.RedoLogOutput)
   */
  protected void serializeData(RedoLogOutput out) throws IOException {
    // nothing to do
  }

  /* (non-Javadoc)
   * @see com.zimbra.cs.redolog.op.RedoableOp#deserializeData(java.io.RedoLogInput)
   */
  protected void deserializeData(RedoLogInput in) throws IOException {
    // nothing to do
  }
}

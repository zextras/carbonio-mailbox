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
package com.zimbra.cs.redolog;

import com.zimbra.cs.redolog.op.RedoableOp;

/**
 * @author jhahm
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Generation - Code and Comments
 */
public class RedoException extends Exception {
  RedoableOp mRedoOp;

  public RedoException(String msg, RedoableOp redoOp) {
    super(msg + " (" + redoOp.toString() + ")");
    mRedoOp = redoOp;
  }

  public RedoableOp getRedoOp() {
    return mRedoOp;
  }
}

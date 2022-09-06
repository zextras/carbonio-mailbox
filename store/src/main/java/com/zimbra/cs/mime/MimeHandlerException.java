// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Apr 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.zimbra.cs.mime;

/**
 * @author schemers
 *     <p>TODO To change the template for this generated type comment go to Window - Preferences -
 *     Java - Code Generation - Code and Comments
 */
public class MimeHandlerException extends Exception {

  public MimeHandlerException(String message) {
    super(message);
  }

  public MimeHandlerException(String message, Throwable cause) {
    super(message, cause);
  }

  public MimeHandlerException(Throwable cause) {
    super(cause.getMessage(), cause);
  }
}

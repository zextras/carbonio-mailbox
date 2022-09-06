// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.extension;

/**
 * This exception is thrown by Zimbra extension classes when they notify an extension specific error
 * to Zimbra extension framework.
 *
 * <p>For example, {@link ZimbraExtension#init()} may throw this exception when it failed its
 * initialization and wants to unregister the extension from the framework.
 *
 * @author ysasaki
 */
public class ExtensionException extends Exception {
  private static final long serialVersionUID = 3703802218451911403L;

  public ExtensionException(String message) {
    super(message);
  }

  public ExtensionException(String message, Throwable cause) {
    super(message, cause);
  }
}

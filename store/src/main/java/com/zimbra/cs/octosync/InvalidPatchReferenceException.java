// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.octosync;

@SuppressWarnings("serial")
public class InvalidPatchReferenceException extends PatchException {
  public InvalidPatchReferenceException() {}

  public InvalidPatchReferenceException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidPatchReferenceException(String message) {
    super(message);
  }

  public InvalidPatchReferenceException(Throwable cause) {
    super(cause);
  }
}

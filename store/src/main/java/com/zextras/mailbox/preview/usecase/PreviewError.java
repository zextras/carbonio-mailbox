// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.preview.usecase;

public class PreviewError extends RuntimeException {

  private static final long serialVersionUID = 3056232713795756943L;

  public PreviewError(String message) {
    super("Preview error: " + message);
  }
}

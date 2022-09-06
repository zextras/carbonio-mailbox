// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.io.IOException;

public final class IndexPendingDeleteException extends IOException {
  private static final long serialVersionUID = -6732022804385197653L;

  public IndexPendingDeleteException() {
    super("Index is pending delete");
  }
}

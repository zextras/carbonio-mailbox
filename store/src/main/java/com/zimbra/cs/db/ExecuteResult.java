// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;
/** SQL execute return wrapper */
public class ExecuteResult<T> {
  public T result;

  public ExecuteResult(T result) {
    this.result = result;
  }

  public T getResult() {
    return result;
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import org.apache.jsieve.mail.Action;

public class ActionEreject implements Action {
  private String fieldMessage;

  public ActionEreject(String fieldMessage) {
    this.fieldMessage = fieldMessage;
  }

  public String getMessage() {
    return fieldMessage;
  }

  public void setMessage(String fieldMessage) {
    this.fieldMessage = fieldMessage;
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import org.apache.jsieve.mail.Action;

/** */
public class ActionReply implements Action {

  private String bodyTemplate;

  public ActionReply(String bodyTemplate) {
    this.bodyTemplate = bodyTemplate;
  }

  public String getBodyTemplate() {
    return bodyTemplate;
  }
}

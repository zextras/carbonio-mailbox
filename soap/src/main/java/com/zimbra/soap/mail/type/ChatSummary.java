// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

public class ChatSummary extends MessageSummary {

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ChatSummary() {
    this((String) null);
  }

  public ChatSummary(String id) {
    super(id);
  }
}

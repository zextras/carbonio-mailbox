// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

public class MsgIdsAttr extends IdsAttr {
  public MsgIdsAttr(String ids) {
    super(ids);
  }

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private MsgIdsAttr() {
    this((String) null);
  }
}

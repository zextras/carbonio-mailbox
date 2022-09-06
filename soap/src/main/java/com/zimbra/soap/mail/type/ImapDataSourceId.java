// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.zimbra.soap.type.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public class ImapDataSourceId extends Id {
  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  protected ImapDataSourceId() {
    this((String) null);
  }

  ImapDataSourceId(String id) {
    super(id);
  }
}

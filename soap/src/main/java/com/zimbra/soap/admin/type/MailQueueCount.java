// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class MailQueueCount {

  /**
   * @zm-api-field-tag queue-name
   * @zm-api-field-description Queue name
   */
  @XmlAttribute(name = AdminConstants.A_NAME /* name */, required = true)
  private final String name;

  /**
   * @zm-api-field-tag file-count
   * @zm-api-field-description Count of the number of files in a queue directory
   */
  @XmlAttribute(name = AdminConstants.A_N /* n */, required = true)
  private final String count;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private MailQueueCount() {
    this((String) null, (String) null);
  }

  public MailQueueCount(String name, String count) {
    this.name = name;
    this.count = count;
  }

  public String getName() {
    return name;
  }

  public String getCount() {
    return count;
  }
}

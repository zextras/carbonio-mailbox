// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ServerMailQueueQuery {

  /**
   * @zm-api-field-tag mta-server
   * @zm-api-field-description MTA Server
   */
  @XmlAttribute(name = AdminConstants.A_NAME /* name */, required = true)
  private final String serverName;

  /**
   * @zm-api-field-description Mail queue query details
   */
  @XmlElement(name = AdminConstants.E_QUEUE /* queue */, required = true)
  private final MailQueueQuery queue;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ServerMailQueueQuery() {
    this((String) null, (MailQueueQuery) null);
  }

  public ServerMailQueueQuery(String serverName, MailQueueQuery queue) {
    this.serverName = serverName;
    this.queue = queue;
  }

  public String getServerName() {
    return serverName;
  }

  public MailQueueQuery getQueue() {
    return queue;
  }
}

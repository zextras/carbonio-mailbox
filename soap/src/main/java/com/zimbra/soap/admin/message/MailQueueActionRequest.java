// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ServerWithQueueAction;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Command to act on invidual queue files. This proxies through to
 *     postsuper. <br>
 *     list-of-ids can be ALL.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_MAIL_QUEUE_ACTION_REQUEST)
public class MailQueueActionRequest {

  /**
   * @zm-api-field-description Server with queue action
   */
  @XmlElement(name = AdminConstants.E_SERVER, required = true)
  private final ServerWithQueueAction server;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private MailQueueActionRequest() {
    this((ServerWithQueueAction) null);
  }

  public MailQueueActionRequest(ServerWithQueueAction server) {
    this.server = server;
  }

  public ServerWithQueueAction getServer() {
    return server;
  }
}

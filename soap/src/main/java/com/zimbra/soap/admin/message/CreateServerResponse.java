// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.ServerInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CREATE_SERVER_RESPONSE)
@XmlType(propOrder = {})
public class CreateServerResponse {

  /**
   * @zm-api-field-description Information about the newly created server
   */
  @XmlElement(name = AdminConstants.E_SERVER)
  private ServerInfo server;

  public CreateServerResponse() {}

  public void setServer(ServerInfo server) {
    this.server = server;
  }

  public ServerInfo getServer() {
    return server;
  }
}

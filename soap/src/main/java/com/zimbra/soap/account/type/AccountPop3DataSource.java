// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.Pop3DataSource;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {})
public class AccountPop3DataSource extends AccountDataSource implements Pop3DataSource {

  /**
   * @zm-api-field-description Specifies whether imported POP3 messages should be left on the server
   *     or deleted.
   */
  @XmlAttribute(name = MailConstants.A_DS_LEAVE_ON_SERVER)
  private ZmBoolean leaveOnServer;

  public AccountPop3DataSource() {}

  public AccountPop3DataSource(Pop3DataSource data) {
    super(data);
    leaveOnServer = ZmBoolean.fromBool(data.isLeaveOnServer());
  }

  @Override
  public Boolean isLeaveOnServer() {
    return ZmBoolean.toBool(leaveOnServer);
  }

  @Override
  public void setLeaveOnServer(Boolean leaveOnServer) {
    this.leaveOnServer = ZmBoolean.fromBool(leaveOnServer);
  }
}

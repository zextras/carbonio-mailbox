// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AccountInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_ADD_GAL_SYNC_DATASOURCE_RESPONSE)
public class AddGalSyncDataSourceResponse {

  /**
   * @zm-api-field-description Account information
   */
  @XmlElement(name = AdminConstants.E_ACCOUNT, required = true)
  private final AccountInfo account;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private AddGalSyncDataSourceResponse() {
    this((AccountInfo) null);
  }

  public AddGalSyncDataSourceResponse(AccountInfo account) {
    this.account = account;
  }

  public AccountInfo getAccount() {
    return account;
  }
}

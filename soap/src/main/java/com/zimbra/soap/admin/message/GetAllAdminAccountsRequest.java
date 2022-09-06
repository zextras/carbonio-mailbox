// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get all Admin accounts
 */
@XmlRootElement(name = AdminConstants.E_GET_ALL_ADMIN_ACCOUNTS_REQUEST)
public class GetAllAdminAccountsRequest {

  /**
   * @zm-api-field-tag apply-cos
   * @zm-api-field-description Apply COS [default 1 (true)]
   */
  @XmlAttribute(name = AdminConstants.A_APPLY_COS, required = false)
  private ZmBoolean applyCos = ZmBoolean.ONE /* true */;

  public GetAllAdminAccountsRequest() {}

  public GetAllAdminAccountsRequest(Boolean applyCos) {
    setApplyCos(applyCos);
  }

  public void setApplyCos(Boolean applyCos) {
    this.applyCos = ZmBoolean.fromBool(applyCos);
  }

  public Boolean isApplyCos() {
    return ZmBoolean.toBool(applyCos);
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required false
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description This request is used by a mobile gateway app/client to
 *     bootstrap/initialize itself.
 */
@XmlRootElement(name = AccountConstants.E_BOOTSTRAP_MOBILE_GATEWAY_APP_REQUEST)
public class BootstrapMobileGatewayAppRequest {

  /**
   * @zm-api-field-tag want-app-token
   * @zm-api-field-description Whether an "anticipatory app account" auth token is desired.<br>
   *     Default is false.
   */
  @XmlAttribute(name = AccountConstants.A_WANT_APP_TOKEN /* wantAppToken */, required = false)
  private ZmBoolean wantAppToken;

  public boolean getWantAppToken() {
    return ZmBoolean.toBool(wantAppToken, false);
  }

  public void setWantAppToken(ZmBoolean wantAppToken) {
    this.wantAppToken = wantAppToken;
  }
}

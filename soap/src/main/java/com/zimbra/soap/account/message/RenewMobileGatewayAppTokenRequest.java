// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required false
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description When the app auth token expires, the app can request a new auth
 *     token.
 */
@XmlRootElement(name = AccountConstants.E_RENEW_MOBILE_GATEWAY_APP_TOKEN_REQUEST)
public class RenewMobileGatewayAppTokenRequest {

  /**
   * @zm-api-field-tag app-id
   * @zm-api-field-description App ID
   */
  @ZimbraUniqueElement
  @XmlElement(name = AccountConstants.E_APP_ID /* appId */, required = true)
  private String appId;

  /**
   * @zm-api-field-tag app-key
   * @zm-api-field-description App secret key
   */
  @ZimbraUniqueElement
  @XmlElement(name = AccountConstants.E_APP_KEY /* appKey */, required = true)
  private String appKey;

  public RenewMobileGatewayAppTokenRequest(String appId, String appKey) {
    this.appId = appId;
    this.appKey = appKey;
  }

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RenewMobileGatewayAppTokenRequest() {
    this(null, null);
  }

  public String getAppId() {
    return appId;
  }

  public String getAppKey() {
    return appKey;
  }
}

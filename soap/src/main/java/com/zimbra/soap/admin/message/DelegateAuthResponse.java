// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.json.jackson.annotate.ZimbraJsonAttribute;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_DELEGATE_AUTH_RESPONSE)
@XmlType(propOrder = {"authToken", "lifetime"})
public class DelegateAuthResponse {
  /**
   * @zm-api-field-description Auth Token
   */
  @XmlElement(name = AdminConstants.E_AUTH_TOKEN, required = true)
  private String authToken;

  /**
   * @zm-api-field-description Life time for the authorization
   */
  @ZimbraJsonAttribute
  @XmlElement(name = AdminConstants.E_LIFETIME, required = true)
  private long lifetime;

  public DelegateAuthResponse() {}

  public DelegateAuthResponse(String authToken) {
    this(authToken, null);
  }

  public DelegateAuthResponse(String authToken, Long lifetime) {
    this.authToken = authToken;
    if (lifetime != null) this.lifetime = lifetime;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setLifetime(long lifetime) {
    this.lifetime = lifetime;
  }

  public long getLifetime() {
    return lifetime;
  }
}

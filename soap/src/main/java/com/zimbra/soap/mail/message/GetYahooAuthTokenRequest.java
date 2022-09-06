// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Yahoo Auth Token
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_YAHOO_AUTH_TOKEN_REQUEST)
public class GetYahooAuthTokenRequest {

  /**
   * @zm-api-field-tag user
   * @zm-api-field-description User
   */
  @XmlAttribute(name = "user", required = true)
  private final String user;

  /**
   * @zm-api-field-tag Password
   * @zm-api-field-description Password
   */
  @XmlAttribute(name = "password", required = true)
  private final String password;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetYahooAuthTokenRequest() {
    this((String) null, (String) null);
  }

  public GetYahooAuthTokenRequest(String user, String password) {
    this.user = user;
    this.password = password;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("user", user).add("password", password);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

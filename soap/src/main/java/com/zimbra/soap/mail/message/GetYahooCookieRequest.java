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
 * @zm-api-command-description Get Yahoo cookie
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_YAHOO_COOKIE_REQUEST)
public class GetYahooCookieRequest {

  /**
   * @zm-api-field-tag user
   * @zm-api-field-description User
   */
  @XmlAttribute(name = "user", required = true)
  private final String user;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetYahooCookieRequest() {
    this((String) null);
  }

  public GetYahooCookieRequest(String user) {
    this.user = user;
  }

  public String getUser() {
    return user;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("user", user);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

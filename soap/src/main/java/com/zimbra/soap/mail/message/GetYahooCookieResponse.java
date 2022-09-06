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

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_YAHOO_COOKIE_RESPONSE)
public class GetYahooCookieResponse {

  /**
   * @zm-api-field-tag error
   * @zm-api-field-description Error
   */
  @XmlAttribute(name = "error", required = false)
  private String error;

  /**
   * @zm-api-field-tag crumb
   * @zm-api-field-description Crumb
   */
  @XmlAttribute(name = "crumb", required = false)
  private String crumb;

  /**
   * @zm-api-field-tag y
   * @zm-api-field-description y
   */
  @XmlAttribute(name = "y", required = false)
  private String y;

  /**
   * @zm-api-field-tag t
   * @zm-api-field-description t
   */
  @XmlAttribute(name = "t", required = false)
  private String t;

  public GetYahooCookieResponse() {}

  public void setError(String error) {
    this.error = error;
  }

  public void setCrumb(String crumb) {
    this.crumb = crumb;
  }

  public void setY(String y) {
    this.y = y;
  }

  public void setT(String t) {
    this.t = t;
  }

  public String getError() {
    return error;
  }

  public String getCrumb() {
    return crumb;
  }

  public String getY() {
    return y;
  }

  public String getT() {
    return t;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("error", error).add("crumb", crumb).add("y", y).add("t", t);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

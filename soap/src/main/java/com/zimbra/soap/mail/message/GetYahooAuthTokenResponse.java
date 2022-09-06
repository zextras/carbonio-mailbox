// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_YAHOO_AUTH_TOKEN_RESPONSE)
public class GetYahooAuthTokenResponse {

  /**
   * @zm-api-field-tag failed
   * @zm-api-field-description Failed
   */
  @XmlAttribute(name = "failed", required = false)
  private ZmBoolean failed;

  public GetYahooAuthTokenResponse() {}

  public void setFailed(Boolean failed) {
    this.failed = ZmBoolean.fromBool(failed);
  }

  public Boolean getFailed() {
    return ZmBoolean.toBool(failed);
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("failed", failed);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

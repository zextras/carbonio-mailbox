// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CHECK_EXCHANGE_AUTH_RESPONSE)
@XmlType(propOrder = {"code", "message"})
public class CheckExchangeAuthResponse {

  /**
   * @zm-api-field-description Code
   */
  @XmlElement(name = AdminConstants.E_CODE, required = true)
  private final String code;

  /**
   * @zm-api-field-description Message
   */
  @XmlElement(name = AdminConstants.E_MESSAGE, required = false)
  private final String message;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CheckExchangeAuthResponse() {
    this((String) null, (String) null);
  }

  public CheckExchangeAuthResponse(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}

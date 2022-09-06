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
@XmlRootElement(name = AdminConstants.E_CHECK_HOSTNAME_RESOLVE_RESPONSE)
@XmlType(propOrder = {"code", "message"})
public class CheckHostnameResolveResponse {

  /**
   * @zm-api-field-description Code
   */
  @XmlElement(name = AdminConstants.E_CODE, required = true)
  private String code;
  /**
   * @zm-api-field-description Message
   */
  @XmlElement(name = AdminConstants.E_MESSAGE, required = false)
  private String message;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CheckHostnameResolveResponse() {
    this((String) null, (String) null);
  }

  private CheckHostnameResolveResponse(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public static CheckHostnameResolveResponse fromCodeMessage(String code, String message) {
    return new CheckHostnameResolveResponse(code, message);
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.Attr;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Config request
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_CONFIG_REQUEST)
@XmlType(propOrder = {})
public class GetConfigRequest {

  /**
   * @zm-api-field-description Attribute
   */
  @XmlElement(name = AdminConstants.E_A, required = false)
  private Attr attr;

  public GetConfigRequest() {}

  public void setAttr(Attr attr) {
    this.attr = attr;
  }

  public Attr getAttr() {
    return attr;
  }
}

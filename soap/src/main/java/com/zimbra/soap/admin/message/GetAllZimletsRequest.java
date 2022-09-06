// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get all Zimlets
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_ALL_ZIMLETS_REQUEST)
public class GetAllZimletsRequest {

  /**
   * @zm-api-field-tag exclude
   * @zm-api-field-description {exclude} can be "none|extension|mail"
   *     <table>
   * <tr> <td> <b>extension</b> </td> <td> return only mail Zimlets </td> </tr>
   * <tr> <td> <b>mail</b> </td> <td> return only admin extensions </td> </tr>
   * <tr> <td> <b>none [default]</b> </td> <td> return both mail and admin zimlets </td> </tr>
   * </table>
   */
  @XmlAttribute(name = AdminConstants.A_EXCLUDE, required = false)
  private final String exclude;

  public GetAllZimletsRequest() {
    this((String) null);
  }

  public GetAllZimletsRequest(String exclude) {
    this.exclude = exclude;
  }

  public String getExclude() {
    return exclude;
  }
}

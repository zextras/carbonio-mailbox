// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Modify an account <br>
 *     Notes:
 *     <ul>
 *       <li>an empty attribute value removes the specified attr
 *       <li>this request is by default proxied to the account's home server
 *     </ul>
 *     <b>Access</b>: domain admin sufficient. limited set of attributes that can be updated by a
 *     domain admin.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_MODIFY_ACCOUNT_REQUEST)
@XmlType(propOrder = {})
public class ModifyAccountRequest extends AdminAttrsImpl {

  /**
   * @zm-api-field-tag value-of-id
   * @zm-api-field-description Zimbra ID
   */
  @XmlAttribute(name = AdminConstants.A_ID, required = true)
  private final String id;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private ModifyAccountRequest() {
    this(null);
  }

  public ModifyAccountRequest(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}

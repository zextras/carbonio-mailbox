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
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Remove Account Alias <br>
 *     <b>Access</b>: domain admin sufficient <br>
 *     note: this request is by default proxied to the account's home server
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_REMOVE_ACCOUNT_ALIAS_REQUEST)
@XmlType(propOrder = {})
public class RemoveAccountAliasRequest {

  /**
   * @zm-api-field-tag value-of-id
   * @zm-api-field-description Zimbra ID
   */
  @XmlAttribute(name = AdminConstants.E_ID, required = false)
  private final String id;

  /**
   * @zm-api-field-description Alias
   */
  @XmlAttribute(name = AdminConstants.E_ALIAS, required = true)
  private final String alias;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RemoveAccountAliasRequest() {
    this(null, null);
  }

  public RemoveAccountAliasRequest(String id, String alias) {
    this.id = id;
    this.alias = alias;
  }

  public String getId() {
    return id;
  }

  public String getAlias() {
    return alias;
  }
}

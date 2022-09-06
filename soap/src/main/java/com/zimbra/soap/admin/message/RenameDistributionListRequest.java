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
 * @zm-api-command-description Rename Distribution List <br>
 *     <b>Access</b>: domain admin sufficient
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_RENAME_DISTRIBUTION_LIST_REQUEST)
public class RenameDistributionListRequest {

  /**
   * @zm-api-field-tag value-of-id
   * @zm-api-field-description Zimbra ID
   */
  @XmlAttribute(name = AdminConstants.E_ID, required = true)
  private final String id;

  /**
   * @zm-api-field-tag new-DL-name
   * @zm-api-field-description New Distribution List name
   */
  @XmlAttribute(name = AdminConstants.E_NEW_NAME, required = true)
  private final String newName;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RenameDistributionListRequest() {
    this((String) null, (String) null);
  }

  public RenameDistributionListRequest(String id, String newName) {
    this.id = id;
    this.newName = newName;
  }

  public String getId() {
    return id;
  }

  public String getNewName() {
    return newName;
  }
}

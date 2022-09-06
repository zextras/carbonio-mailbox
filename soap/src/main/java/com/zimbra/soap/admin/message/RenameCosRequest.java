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

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Rename Class of Service (COS)
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_RENAME_COS_REQUEST)
@XmlType(propOrder = {})
public class RenameCosRequest {

  /**
   * @zm-api-field-tag value-of-id
   * @zm-api-field-description Zimbra ID
   */
  @XmlElement(name = AdminConstants.A_ID, required = true)
  private final String id;

  /**
   * @zm-api-field-tag new-cos-name
   * @zm-api-field-description new COS name
   */
  @XmlElement(name = AdminConstants.E_NEW_NAME, required = true)
  private final String newName;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RenameCosRequest() {
    this(null, null);
  }

  public RenameCosRequest(String id, String newName) {
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

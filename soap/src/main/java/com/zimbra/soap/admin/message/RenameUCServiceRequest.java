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
 * @zm-api-command-description Rename Unified Communication Service
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_RENAME_UC_SERVICE_REQUEST)
@XmlType(propOrder = {})
public class RenameUCServiceRequest {

  /**
   * @zm-api-field-tag value-of-id
   * @zm-api-field-description Zimbra ID
   */
  @XmlElement(name = AdminConstants.E_ID, required = true)
  private final String id;

  /**
   * @zm-api-field-tag new-uc-service-name
   * @zm-api-field-description new UC Service name
   */
  @XmlElement(name = AdminConstants.E_NEW_NAME, required = true)
  private final String newName;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private RenameUCServiceRequest() {
    this(null, null);
  }

  public RenameUCServiceRequest(String id, String newName) {
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

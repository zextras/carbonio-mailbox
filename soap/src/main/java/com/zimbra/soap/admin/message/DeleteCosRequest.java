// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Delete a Class of Service (COS)
 */
@XmlRootElement(name = AdminConstants.E_DELETE_COS_REQUEST)
public class DeleteCosRequest {

  /**
   * @zm-api-field-tag value-of-id
   * @zm-api-field-description Zimbra ID
   */
  @XmlElement(name = AdminConstants.A_ID, required = true)
  private final String id;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DeleteCosRequest() {
    this(null);
  }

  public DeleteCosRequest(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}

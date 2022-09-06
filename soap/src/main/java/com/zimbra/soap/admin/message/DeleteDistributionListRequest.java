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
 * @zm-api-command-description Delete a distribution list <br>
 *     <b>Access</b>: domain admin sufficient
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_DELETE_DISTRIBUTION_LIST_REQUEST)
public class DeleteDistributionListRequest {

  /**
   * @zm-api-field-tag value-of-id
   * @zm-api-field-description Zimbra ID
   */
  @XmlAttribute(name = AdminConstants.E_ID, required = true)
  private final String id;

  /**
   * @zm-api-field-tag cascadeDelete
   * @zm-api-field-description If true, cascade delete the hab-groups else return error
   */
  @XmlAttribute(name = AdminConstants.A_CASCADE_DELETE, required = false)
  private final boolean cascadeDelete;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private DeleteDistributionListRequest() {
    this(null);
  }

  public DeleteDistributionListRequest(String id) {
    this(id, false);
  }

  public DeleteDistributionListRequest(String id, boolean cascadeDelete) {
    this.id = id;
    this.cascadeDelete = cascadeDelete;
  }

  public String getId() {
    return id;
  }

  public boolean isCascadeDelete() {
    return cascadeDelete;
  }
}

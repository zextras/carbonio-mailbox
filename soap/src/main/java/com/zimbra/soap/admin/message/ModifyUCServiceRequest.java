// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Modify attributes for a UC service <br>
 *     Notes:
 *     <ul>
 *       <li>an empty attribute value removes the specified attr
 *     </ul>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_MODIFY_UC_SERVICE_REQUEST)
@XmlType(propOrder = {})
public class ModifyUCServiceRequest extends AdminAttrsImpl {

  /**
   * @zm-api-field-tag value-of-id
   * @zm-api-field-description Zimbra ID
   */
  @XmlElement(name = AdminConstants.E_ID, required = true)
  private String id;

  public ModifyUCServiceRequest() {}

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}

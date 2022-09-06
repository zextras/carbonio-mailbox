// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.HABGroupOperation;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Modify Hierarchical address book group parent <br>
 *     <b>Access</b>: domain admin sufficient
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_MODIFY_HAB_GROUP_REQUEST)
public class ModifyHABGroupRequest {

  /**
   * @zm-api-field-description Tokens
   */
  @XmlElement(name = AdminConstants.E_HAB_GROUP_OPERATION /* token */, required = true)
  private HABGroupOperation operation;

  public ModifyHABGroupRequest() {}

  public ModifyHABGroupRequest(HABGroupOperation operation) {
    this.operation = operation;
  }

  public HABGroupOperation getOperation() {
    return operation;
  }

  public void setOperation(HABGroupOperation operation) {
    this.operation = operation;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ModifyHABGroupRequest [");
    if (operation != null) {
      builder.append("operation=");
      builder.append(operation);
    }
    builder.append("]");
    return builder.toString();
  }
}

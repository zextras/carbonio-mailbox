// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author zimbra
 */
/**
 * @zm-api-command-auth-required true
 * @zm-api-command-description Get the groups in a HAB org unit.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_GET_HAB_REQUEST)
@XmlType(propOrder = {})
public class GetHABRequest {

  /**
   * @zm-api-field-tag name
   * @zm-api-field-description HAB root group zimbra Id
   */
  @XmlAttribute(name = AccountConstants.A_HAB_ROOT_GROUP_ID /* habRootGroupId */, required = true)
  private String habRootGroupId;

  public GetHABRequest() {}

  public GetHABRequest(String rootGrpId) {
    this.habRootGroupId = rootGrpId;
  }

  public String getHabRootGroupId() {
    return habRootGroupId;
  }

  public void setHabRootGroupId(String habRootGroupId) {
    this.habRootGroupId = habRootGroupId;
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.UCServiceInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_MODIFY_UC_SERVICE_RESPONSE)
@XmlType(propOrder = {})
public class ModifyUCServiceResponse {

  /**
   * @zm-api-field-description Information about ucservice
   */
  @XmlElement(name = AdminConstants.E_UC_SERVICE)
  private UCServiceInfo ucService;

  public ModifyUCServiceResponse() {}

  public void setUCService(UCServiceInfo ucService) {
    this.ucService = ucService;
  }

  public UCServiceInfo getUCService() {
    return ucService;
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CosInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_RENAME_COS_RESPONSE)
@XmlType(propOrder = {})
public class RenameCosResponse {

  /**
   * @zm-api-field-description Information about Class Of Service (COS)
   */
  @XmlElement(name = AdminConstants.E_COS, required = true)
  private CosInfo cos;

  public RenameCosResponse() {}

  public void setCos(CosInfo cos) {
    this.cos = cos;
  }

  public CosInfo getCos() {
    return cos;
  }
}

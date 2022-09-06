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

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_COS_RESPONSE)
public class GetCosResponse {

  /**
   * @zm-api-field-description Information about Class of Service (COS)
   */
  @XmlElement(name = AdminConstants.E_COS)
  private CosInfo cos;

  public GetCosResponse() {}

  public void setCos(CosInfo cos) {
    this.cos = cos;
  }

  public CosInfo getCos() {
    return cos;
  }
}

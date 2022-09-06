// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CosSelector;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_CLEAR_TWO_FACTOR_AUTH_DATA_STATUS_REQUEST)
public class GetClearTwoFactorAuthDataStatusRequest {
  @XmlElement(name = AdminConstants.E_COS, required = false)
  private CosSelector cos;

  private GetClearTwoFactorAuthDataStatusRequest() {}

  public GetClearTwoFactorAuthDataStatusRequest(CosSelector cos) {
    setCos(cos);
  }

  public void setCos(CosSelector cos) {
    this.cos = cos;
  }

  public CosSelector getCos() {
    return cos;
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.message;

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.soap.account.type.DLInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AccountConstants.E_CREATE_DISTRIBUTION_LIST_RESPONSE)
public class CreateDistributionListResponse {

  /**
   * @zm-api-field-description Information about created distribution list
   */
  @XmlElement(name = AccountConstants.E_DL, required = true)
  private final DLInfo dl;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CreateDistributionListResponse() {
    this((DLInfo) null);
  }

  public CreateDistributionListResponse(DLInfo dl) {
    this.dl = dl;
  }

  public DLInfo getDl() {
    return dl;
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.RightViaInfo;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_CHECK_RIGHT_RESPONSE)
public class CheckRightResponse {

  /**
   * @zm-api-field-description Result of the CheckRightRequest
   */
  @XmlAttribute(name = AdminConstants.A_ALLOW /* allow */, required = true)
  private final ZmBoolean allow;

  /**
   * @zm-api-field-description Via information for the grant that decisively lead to the result
   */
  @XmlElement(name = AdminConstants.E_VIA /* via */, required = false)
  private final RightViaInfo via;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private CheckRightResponse() {
    this(false, (RightViaInfo) null);
  }

  public CheckRightResponse(boolean allow, RightViaInfo via) {
    this.allow = ZmBoolean.fromBool(allow);
    this.via = via;
  }

  public boolean getAllow() {
    return ZmBoolean.toBool(allow);
  }

  public RightViaInfo getVia() {
    return via;
  }
}

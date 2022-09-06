// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Service Status
 */
@XmlRootElement(name = AdminConstants.E_GET_SERVICE_STATUS_REQUEST)
public class GetServiceStatusRequest {

  public ZmBoolean getCarbonioServicesOnly() {
    return carbonioServicesOnly;
  }

  /**
   * @zm-api-field-description carbonioServicesOnly. If <b>1 (true)</b>, only return carbonio
   *     services status.
   */
  @XmlAttribute(name = AdminConstants.A_CARBONIO_SERVICES_ONLY, required = false)
  private ZmBoolean carbonioServicesOnly;

  public GetServiceStatusRequest() {}
}

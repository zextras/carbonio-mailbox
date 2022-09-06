// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.UCServiceInfo;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = AdminConstants.E_GET_ALL_UC_SERVICES_RESPONSE)
public class GetAllUCServicesResponse {

  /**
   * @zm-api-field-description Information about uc services
   */
  @XmlElement(name = AdminConstants.E_UC_SERVICE)
  private List<UCServiceInfo> ucServiceList = new ArrayList<UCServiceInfo>();

  public GetAllUCServicesResponse() {}

  public void addUCService(UCServiceInfo ucService) {
    this.getUCServiceList().add(ucService);
  }

  public List<UCServiceInfo> getUCServiceList() {
    return ucServiceList;
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.ServerSelector;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get all calendar resources that match the selection criteria <br>
 *     <b>Access</b>: domain admin sufficient
 */
@XmlRootElement(name = AdminConstants.E_GET_ALL_CALENDAR_RESOURCES_REQUEST)
public class GetAllCalendarResourcesRequest extends GetAllAccountsRequest {

  public GetAllCalendarResourcesRequest() {
    super();
  }

  public GetAllCalendarResourcesRequest(ServerSelector server, DomainSelector domain) {
    super(server, domain);
  }
}

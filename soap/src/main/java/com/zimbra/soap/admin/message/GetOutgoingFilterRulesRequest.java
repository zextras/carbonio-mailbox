// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.ServerSelector;
import com.zimbra.soap.type.AccountSelector;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get filter rules
 */
@XmlRootElement(name = AdminConstants.E_GET_OUTGOING_FILTER_RULES_REQUEST)
public class GetOutgoingFilterRulesRequest extends GetFilterRulesRequest {
  public GetOutgoingFilterRulesRequest() {
    super();
  }

  public GetOutgoingFilterRulesRequest(AccountSelector accountSelector, String type) {
    super(accountSelector, type);
  }

  public GetOutgoingFilterRulesRequest(DomainSelector domainSelector, String type) {
    super(domainSelector, type);
  }

  public GetOutgoingFilterRulesRequest(CosSelector cosSelector, String type) {
    super(cosSelector, type);
  }

  public GetOutgoingFilterRulesRequest(ServerSelector serverSelector, String type) {
    super(serverSelector, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("GetOutgoingFilterRulesRequest ");
    sb.append(super.getToStringData());
    return sb.toString();
  }
}

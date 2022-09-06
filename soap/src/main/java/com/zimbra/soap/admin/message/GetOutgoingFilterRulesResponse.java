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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_GET_OUTGOING_FILTER_RULES_RESPONSE)
public final class GetOutgoingFilterRulesResponse extends GetFilterRulesResponse {
  public GetOutgoingFilterRulesResponse() {
    super();
  }

  public GetOutgoingFilterRulesResponse(String type) {
    super(type);
  }

  public GetOutgoingFilterRulesResponse(String type, AccountSelector accountSelector) {
    super(type, accountSelector);
  }

  public GetOutgoingFilterRulesResponse(String type, DomainSelector domainSelector) {
    super(type, domainSelector);
  }

  public GetOutgoingFilterRulesResponse(String type, CosSelector cosSelector) {
    super(type, cosSelector);
  }

  public GetOutgoingFilterRulesResponse(String type, ServerSelector serverSelector) {
    super(type, serverSelector);
  }
}

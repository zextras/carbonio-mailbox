// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.filter.RuleManager;
import com.zimbra.cs.filter.RuleManager.FilterType;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.admin.message.GetOutgoingFilterRulesRequest;
import com.zimbra.soap.admin.message.GetOutgoingFilterRulesResponse;
import java.util.Map;

public final class GetOutgoingFilterRules extends GetFilterRules {

  @Override
  public Element handle(Element req, Map<String, Object> context) throws ServiceException {
    zsc = getZimbraSoapContext(context);
    GetOutgoingFilterRulesRequest request = JaxbUtil.elementToJaxb(req);

    setAFTypeSelectorsAndEntry(request, zsc);

    if (acctSel != null) {
      entry = verifyAccountHarvestingAndPerms(acctSel, zsc);
      resp = new GetOutgoingFilterRulesResponse(afType.getType(), acctSel);
    } else if (domainSelector != null) {
      entry = verifyDomainPerms(domainSelector, zsc);
      resp = new GetOutgoingFilterRulesResponse(afType.getType(), domainSelector);
    } else if (cosSelector != null) {
      entry = verifyCosPerms(cosSelector, zsc);
      resp = new GetOutgoingFilterRulesResponse(afType.getType(), cosSelector);
    } else if (serverSelector != null) {
      entry = verifyServerPerms(serverSelector, zsc);
      resp = new GetOutgoingFilterRulesResponse(afType.getType(), serverSelector);
    } else {
      // one of the selector must be present
      throw ServiceException.INVALID_REQUEST("Selector not provided.", null);
    }

    rules = RuleManager.getAdminRulesAsXML(entry, FilterType.OUTGOING, afType);
    resp.addFilterRules(rules);
    return zsc.jaxbToElement(resp);
  }
}

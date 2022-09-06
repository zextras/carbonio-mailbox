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
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.ModifyOutgoingFilterRulesRequest;
import com.zimbra.soap.admin.message.ModifyOutgoingFilterRulesResponse;
import java.util.Map;

public final class ModifyOutgoingFilterRules extends ModifyFilterRules {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    ModifyOutgoingFilterRulesRequest req = JaxbUtil.elementToJaxb(request);

    setAFTypeAndSelectors(req);

    if (acctSel != null) {
      entry = verifyAccountHarvestingAndPerms(acctSel, zsc);
    } else if (domainSelector != null) {
      entry = verifyDomainPerms(domainSelector, zsc);
    } else if (cosSelector != null) {
      entry = verifyCosPerms(cosSelector, zsc);
    } else if (serverSelector != null) {
      entry = verifyServerPerms(serverSelector, zsc);
    } else {
      // one of the selector must be present
      throw ServiceException.INVALID_REQUEST("Selector not provided.", null);
    }

    RuleManager.setAdminRulesFromXML(entry, req.getFilterRules(), FilterType.OUTGOING, afType);
    return zsc.jaxbToElement(new ModifyOutgoingFilterRulesResponse());
  }
}

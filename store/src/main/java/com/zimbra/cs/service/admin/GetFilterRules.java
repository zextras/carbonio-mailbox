// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.filter.RuleManager;
import com.zimbra.cs.filter.RuleManager.AdminFilterType;
import com.zimbra.cs.filter.RuleManager.FilterType;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetFilterRulesRequest;
import com.zimbra.soap.admin.message.GetFilterRulesResponse;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.ServerSelector;
import com.zimbra.soap.mail.type.FilterRule;
import com.zimbra.soap.type.AccountSelector;
import java.util.List;
import java.util.Map;

public class GetFilterRules extends AdminDocumentHandler {
  protected Entry entry = null;
  protected AdminFilterType afType = null;
  protected AccountSelector acctSel = null;
  protected DomainSelector domainSelector = null;
  protected CosSelector cosSelector = null;
  protected ServerSelector serverSelector = null;
  protected List<FilterRule> rules = null;
  protected ZimbraSoapContext zsc = null;
  protected GetFilterRulesResponse resp = null;

  @Override
  public Element handle(Element req, Map<String, Object> context) throws ServiceException {
    zsc = getZimbraSoapContext(context);
    GetFilterRulesRequest request = JaxbUtil.elementToJaxb(req);

    setAFTypeSelectorsAndEntry(request, zsc);

    if (acctSel != null) {
      entry = verifyAccountHarvestingAndPerms(acctSel, zsc);
      resp = new GetFilterRulesResponse(afType.getType(), acctSel);
    } else if (domainSelector != null) {
      entry = verifyDomainPerms(domainSelector, zsc);
      resp = new GetFilterRulesResponse(afType.getType(), domainSelector);
    } else if (cosSelector != null) {
      entry = verifyCosPerms(cosSelector, zsc);
      resp = new GetFilterRulesResponse(afType.getType(), cosSelector);
    } else if (serverSelector != null) {
      entry = verifyServerPerms(serverSelector, zsc);
      resp = new GetFilterRulesResponse(afType.getType(), serverSelector);
    } else {
      // one of the selector must be present
      throw ServiceException.INVALID_REQUEST("Selector not provided.", null);
    }

    rules = RuleManager.getAdminRulesAsXML(entry, FilterType.INCOMING, afType);
    for (FilterRule rule : rules) {
      if (rule.getFilterTests() != null) {
        if (rule.getFilterTests().getTests() == null) {
          rule.setFilterTests(null);
        } else if (rule.getFilterTests().getTests().isEmpty()) {
          rule.setFilterTests(null);
        }
      }
    }
    resp.addFilterRules(rules);
    return zsc.jaxbToElement(resp);
  }

  protected void setAFTypeSelectorsAndEntry(GetFilterRulesRequest request, ZimbraSoapContext zsc)
      throws ServiceException {
    String type = request.getType();
    if (StringUtil.isNullOrEmpty(type)) {
      throw ServiceException.INVALID_REQUEST(
          "Type must be provided in request. Type can be either BEFORE or AFTER.", null);
    } else if (AdminFilterType.AFTER.getType().equalsIgnoreCase(type)) {
      afType = AdminFilterType.AFTER;
    } else if (AdminFilterType.BEFORE.getType().equalsIgnoreCase(type)) {
      afType = AdminFilterType.BEFORE;
    } else {
      throw ServiceException.INVALID_REQUEST(
          "Invalid type provided in request. Type can be either BEFORE or AFTER.", null);
    }

    acctSel = request.getAccount();
    domainSelector = request.getDomain();
    cosSelector = request.getCos();
    serverSelector = request.getServer();
  }
}

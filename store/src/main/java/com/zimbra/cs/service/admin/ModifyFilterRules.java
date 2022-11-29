// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.filter.RuleManager;
import com.zimbra.cs.filter.RuleManager.AdminFilterType;
import com.zimbra.cs.filter.RuleManager.FilterType;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.ModifyFilterRulesRequest;
import com.zimbra.soap.admin.message.ModifyFilterRulesResponse;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.ServerSelector;
import com.zimbra.soap.type.AccountSelector;

public class ModifyFilterRules extends AdminDocumentHandler {
    protected Entry entry = null;
    protected AdminFilterType afType = null;
    protected ZimbraSoapContext zsc = null;
    protected AccountSelector acctSel = null;
    protected DomainSelector domainSelector = null;
    protected CosSelector cosSelector = null;
    protected ServerSelector serverSelector = null;

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        zsc = getZimbraSoapContext(context);
        ModifyFilterRulesRequest req = JaxbUtil.elementToJaxb(request);

        setAFTypeAndSelectors(req);
        if(acctSel != null) {
            entry = verifyAccountHarvestingAndPerms(acctSel, zsc);
        } else if(domainSelector != null) {
            entry = verifyDomainPerms(domainSelector, zsc);
        } else if(cosSelector != null) {
            entry = verifyCosPerms(cosSelector, zsc);
        } else if(serverSelector != null) {
            entry = verifyServerPerms(serverSelector, zsc);
        } else {
            // one of the selector must be present
            throw ServiceException.INVALID_REQUEST("Selector not provided.", null);
        }

        RuleManager.setAdminRulesFromXML(entry, req.getFilterRules(), FilterType.INCOMING, afType);
        return zsc.jaxbToElement(new ModifyFilterRulesResponse());
    }

    protected void setAFTypeAndSelectors(ModifyFilterRulesRequest request) throws ServiceException {
        String type = request.getType();
        if(StringUtil.isNullOrEmpty(type)) {
            throw ServiceException.INVALID_REQUEST("Type must be provided in request. Type can be either BEFORE or AFTER.", null);
        } else if(AdminFilterType.AFTER.getType().equalsIgnoreCase(type)) {
            this.afType = AdminFilterType.AFTER;
        } else if(AdminFilterType.BEFORE.getType().equalsIgnoreCase(type)) {
            this.afType = AdminFilterType.BEFORE;
        } else {
            throw ServiceException.INVALID_REQUEST("Invalid type provided in request. Type can be either BEFORE or AFTER.", null);
        }

        acctSel = request.getAccount();
        domainSelector = request.getDomain();
        cosSelector = request.getCos();
        serverSelector = request.getServer();
    }
}

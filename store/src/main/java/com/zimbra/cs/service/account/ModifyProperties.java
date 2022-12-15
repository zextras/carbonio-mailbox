// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.cs.zimlet.ZimletUserProperties;

/**
 * @author jylee
 */
public class ModifyProperties extends AccountDocumentHandler {

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {
		ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);
        if (!canModifyOptions(zsc, account)) {
            throw ServiceException.PERM_DENIED("can not modify options");
        }
        ZimletUserProperties props = ZimletUserProperties.getProperties(account);
        int numUserproperties = request.listElements(AccountConstants.E_PROPERTY).size();
        if (numUserproperties > account.getLongAttr(Provisioning.A_zimbraZimletUserPropertiesMaxNumEntries, 150)) {
            throw AccountServiceException.TOO_MANY_ZIMLETUSERPROPERTIES();
        }
        for (Element e : request.listElements(AccountConstants.E_PROPERTY)) {
            props.setProperty(e.getAttribute(AccountConstants.A_ZIMLET),
                    e.getAttribute(AccountConstants.A_NAME),
                    e.getText());
            }
        props.saveProperties(account);
        Element response = zsc.createElement(AccountConstants.MODIFY_PROPERTIES_RESPONSE);
        return response;
	}
}

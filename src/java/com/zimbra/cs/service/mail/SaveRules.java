/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2005, 2006 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

/*
 * Created on Jan 7, 2005
 */
package com.zimbra.cs.service.mail;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.filter.RuleManager;
import com.zimbra.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author kchen
 */
public class SaveRules extends MailDocumentHandler {

    /* (non-Javadoc)
     * @see com.zimbra.soap.DocumentHandler#handle(org.dom4j.Element, java.util.Map)
     */
    public Element handle(Element document, Map<String, Object> context)
            throws ServiceException {
        ZimbraSoapContext lc = getZimbraSoapContext(context);
        // FIXME: need to check that account exists
        Account account = getRequestedAccount(lc);
        
        RuleManager mgr = RuleManager.getInstance();
        Element rulesElem = document.getElement(MailService.E_RULES);
        mgr.setXMLRules(account, rulesElem);
        
        Element response = lc.createElement(MailService.SAVE_RULES_RESPONSE);
        return response;
    }

}

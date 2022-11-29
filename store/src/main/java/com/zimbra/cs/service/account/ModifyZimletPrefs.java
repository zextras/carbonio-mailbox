// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import java.util.HashMap;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.zimlet.ZimletPresence;
import com.zimbra.cs.zimlet.ZimletPresence.Presence;
import com.zimbra.cs.zimlet.ZimletUtil;
import com.zimbra.soap.ZimbraSoapContext;

public class ModifyZimletPrefs extends AccountDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context)
            throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);

        if (!canModifyOptions(zsc, account))
            throw ServiceException.PERM_DENIED("can not modify options");

        Map<String, Object> attrs = new HashMap<String, Object>();
        
        ZimletPresence availZimlets = ZimletUtil.getAvailableZimlets(account);
        
        String addEnabled = "+" + Provisioning.A_zimbraPrefZimlets;
        String delEnabled = "-" + Provisioning.A_zimbraPrefZimlets;
        String addDisabled = "+" + Provisioning.A_zimbraPrefDisabledZimlets;
        String delDisabled = "-" + Provisioning.A_zimbraPrefDisabledZimlets;

        for (Element eZimlet : request.listElements(AccountConstants.E_ZIMLET)) {
            String zimletName = eZimlet.getAttribute(AccountConstants.A_NAME);
            String presense = eZimlet.getAttribute(AccountConstants.A_ZIMLET_PRESENCE);
            Presence userPrefPresence = Presence.fromString(presense);
            
            // user cannot make a zimlet mandatory
            if (userPrefPresence == Presence.mandatory)
                throw ServiceException.INVALID_REQUEST("invalid zimlet presence: " + presense, null);
            
            Presence defaultPresence = availZimlets.getPresence(zimletName);
            
            /*
             * if zimlet is not available to the user or is mandatory,
             * we don't want it to appear in either enabled/disabled prefs,
             * regardless what the user wants.
             * 
             * if default presence is the same as what user wants. it does not 
             * need to appear in the user pref.
             */
            if (defaultPresence == null ||               // zimlet not available to the user
                defaultPresence == Presence.mandatory || // zimlet is mandatory
                defaultPresence == userPrefPresence) {   // default == what user wants
                StringUtil.addToMultiMap(attrs, delEnabled, zimletName);
                StringUtil.addToMultiMap(attrs, delDisabled, zimletName);
            } else {
                /*
                 * default != what user wants
                 */
                if (userPrefPresence == Presence.enabled) {
                    // user wants the zimlet enabled
                    StringUtil.addToMultiMap(attrs, addEnabled, zimletName);
                    StringUtil.addToMultiMap(attrs, delDisabled, zimletName);
                } else {
                    // user wants the zimlet disabled
                    StringUtil.addToMultiMap(attrs, delEnabled, zimletName);
                    StringUtil.addToMultiMap(attrs, addDisabled, zimletName);
                }    
            }
        }
        
        Provisioning prov = Provisioning.getInstance();
        prov.modifyAttrs(account, attrs);

        Element response = zsc.createElement(AccountConstants.MODIFY_ZIMLET_PREFS_RESPONSE);
        doResponse(prov, response, account);
        return response;
    }

    private void doResponse(Provisioning prov, Element response, Account acct) throws ServiceException {
        ZimletPresence userZimlets = ZimletUtil.getUserZimlets(acct);
        for (String zimletName : userZimlets.getZimletNames()) {
            Zimlet zimlet = prov.getZimlet(zimletName);
            if (zimlet != null && zimlet.isEnabled() && !zimlet.isExtension()) {
                Element eZimlet = response.addElement(AccountConstants.E_ZIMLET);
                eZimlet.addAttribute(AccountConstants.A_NAME, zimletName);
                eZimlet.addAttribute(AccountConstants.A_ZIMLET_PRESENCE, userZimlets.getPresence(zimletName).toString());
            }
        }
    }
}

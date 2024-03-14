// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.account;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.calendar.TZIDMapper;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author schemers
 */
public class GetPrefs extends AccountDocumentHandler  {

	public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);

        if (!canAccessAccount(zsc, account))
            throw ServiceException.PERM_DENIED("can not access account");

        Element response = zsc.createElement(AccountConstants.GET_PREFS_RESPONSE);
        handle(request, response, account);
        return response;
    }

    /**
     * Passes in a request that optionally has &lt;pref&gt; items as a filter and fills in the response document with
     * gathered preferences. This method extracts preferences from the request and populates the response document
     * accordingly.
     *
     * @param request  The XML element containing optional <pref> items.
     * @param response The XML element to be filled with gathered preferences.
     * @param acct     The account for which preferences are being handled.
     * @throws ServiceException If an error occurs while handling preferences.
     */
    public static void handle(Element request, Element response, Account acct) throws ServiceException {
        HashSet<String> specificPrefs = null;

        for (Element epref : request.listElements(AccountConstants.E_PREF)) {
            if (specificPrefs == null) {
                specificPrefs = new HashSet<>();
            }
            specificPrefs.add(epref.getAttribute(AccountConstants.A_NAME));
        }

        Map<String, Object> map = acct.getUnicodeAttrs();
        if (map != null) {
            doPrefs(acct, response, map, specificPrefs);
        }
    }

    /**
     * Populates the preferences element with the specified account's preferences based on the provided attribute map.
     *
     * @param acct The account whose preferences are being processed.
     * @param prefs The element to populate with preferences.
     * @param attrsMap A map containing the account's attribute preferences.
     * @param specificPrefs A set of specific preferences to include, or null to include all preferences.
     */
    public static void doPrefs(Account acct, Element prefs, Map<String, Object> attrsMap, Set<String> specificPrefs) {
        for (Map.Entry<String, Object> entry : attrsMap.entrySet()) {
            String key = entry.getKey();

            if (specificPrefs != null && !specificPrefs.contains(key))
                continue;

            if (key.startsWith("zimbraPref") || key.startsWith("carbonioPref")) {
                Object value = entry.getValue();

                if (value instanceof String[]) {
                    String[] sa = (String[]) value;
                    for (String s : sa)
                        prefs.addKeyValuePair(key, s, AccountConstants.E_PREF, AccountConstants.A_NAME);
                } else {
                    if (key.equals(ZAttrProvisioning.A_zimbraPrefTimeZoneId))
                        value = TZIDMapper.canonicalize((String) value);

                    if ((key.equals(ZAttrProvisioning.A_zimbraPrefImapEnabled) && !acct.isImapEnabled()) ||
                        (key.equals(ZAttrProvisioning.A_zimbraPrefPop3Enabled) && !acct.isPop3Enabled()))
                        continue;

                    prefs.addKeyValuePair(key, (String) value, AccountConstants.E_PREF, AccountConstants.A_NAME);
                }
            }
        }
    }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.cs.account.accesscontrol.UserRight;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author pshao
 */
public class DiscoverRights extends AccountDocumentHandler {

    /* can't do this, RightManager might not have been initialized
    private static final Set<? extends Right> DELEGATED_SEND_RIGHTS = 
        Sets.newHashSet(
                User.R_sendAs,
                User.R_sendOnBehalfOf,
                User.R_sendAsDistList,
                User.R_sendOnBehalfOfDistList);
    
    */
    
    private static final Set<String> DELEGATED_SEND_RIGHTS = 
        Sets.newHashSet(
                Right.RT_sendAs,
                Right.RT_sendOnBehalfOf,
                Right.RT_sendAsDistList,
                Right.RT_sendOnBehalfOfDistList);
    
    
    @Override
    public Element handle(Element request, Map<String, Object> context)
            throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Account account = getRequestedAccount(zsc);

        if (!canAccessAccount(zsc, account)) {
            throw ServiceException.PERM_DENIED("can not access account");
        }
        
        RightManager rightMgr = RightManager.getInstance();
        Set<Right> rights = Sets.newHashSet();
        for (Element eRight : request.listElements(AccountConstants.E_RIGHT)) {
            UserRight r = rightMgr.getUserRight(eRight.getText());
            rights.add(r); 
        }
        
        if (rights.size() == 0) {
            throw ServiceException.INVALID_REQUEST("no right is specified", null);
        }

        Element response = zsc.createElement(AccountConstants.DISCOVER_RIGHTS_RESPONSE);
        discoverRights(account, rights, response, true);

        return response;
    }
    
    public static boolean isDelegatedSendRight(Right right) {
        return DELEGATED_SEND_RIGHTS.contains(right.getName());
    }
    
    public static void discoverRights(Account account, Set<Right> rights, Element eParent,
            boolean onMaster) throws ServiceException {
        AccessManager accessMgr = AccessManager.getInstance();
        Map<Right, Set<Entry>> discoveredRights = accessMgr.discoverUserRights(account, rights, onMaster);
        
        Locale locale = account.getLocale();
        for (Map.Entry<Right, Set<Entry>> targetsForRight : discoveredRights.entrySet()) {
            Right right = targetsForRight.getKey();
            Set<Entry> targets = targetsForRight.getValue();
            
            List<Entry> sortedTargets = Entry.sortByDisplayName(targets, locale);
            
            boolean isDelegatedSendRight = isDelegatedSendRight(right);
            
            Element eTargets = eParent.addElement(AccountConstants.E_TARGETS);
            eTargets.addAttribute(AccountConstants.A_RIGHT, right.getName());
            for (Entry target : sortedTargets) {
                TargetType targetType = TargetType.getTargetType(target);
                Element eTarget = eTargets.addElement(AccountConstants.E_TARGET);
                eTarget.addAttribute(AccountConstants.A_TYPE, targetType.getCode());
                if (isDelegatedSendRight) {
                    if (target instanceof Account || target instanceof Group) {
                        String[] addrs = AccountUtil.getAllowedSendAddresses((NamedEntry) target);
                        NamedEntry entry = (NamedEntry) target;
                        for (String addr : addrs) {
                            Element eEmail = eTarget.addElement(AccountConstants.E_EMAIL);
                            eEmail.addAttribute(AccountConstants.A_ADDR, addr);
                        }
                        if (target instanceof Account) {
                            eTarget.addAttribute(AccountConstants.A_DISPLAY, ((Account) entry).getDisplayName());
                        } else if (target instanceof Group) {
                            eTarget.addAttribute(AccountConstants.A_DISPLAY, ((Group) entry).getDisplayName());
                        }
                    } else {
                        throw ServiceException.FAILURE("internal error, target for " +
                                " delegated send rights must be account or group", null);
                    }
                } else {
                    if (target instanceof NamedEntry) {
                        NamedEntry entry = (NamedEntry) target;
                        eTarget.addAttribute(AccountConstants.A_ID, entry.getId());
                        eTarget.addAttribute(AccountConstants.A_NAME, entry.getName());
                        if (target instanceof Account) {
                            eTarget.addAttribute(AccountConstants.A_DISPLAY, ((Account) entry).getDisplayName());
                        } else if (target instanceof Group) {
                            eTarget.addAttribute(AccountConstants.A_DISPLAY, ((Group) entry).getDisplayName());
                        }
                    } else {
                        eTarget.addAttribute(AccountConstants.A_NAME, target.getLabel());
                    }
                }
            }
        }
    }

}

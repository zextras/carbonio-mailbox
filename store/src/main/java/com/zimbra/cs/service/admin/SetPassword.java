// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.Key.CalendarResourceBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.SetPasswordResult;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.SetPasswordRequest;
import com.zimbra.soap.admin.message.SetPasswordResponse;

/**
 * @author schemers
 */
public class SetPassword extends AdminDocumentHandler {

    private static final String[] TARGET_ACCOUNT_PATH = new String[] { AdminConstants.E_ID };
    @Override
    protected String[] getProxiedAccountPath()  { return TARGET_ACCOUNT_PATH; }

    /**
     * must be careful and only allow on accounts domain admin has access to
     */
    @Override
    public boolean domainAuthSufficient(Map context) {
        return true;
    }

    /**
     * @return true - which means accept responsibility for measures to prevent account harvesting by delegate admins
     */
    @Override
    public boolean defendsAgainstDelegateAdminAccountHarvesting() {
        return true;
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        SetPasswordResponse resp = null;
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Provisioning prov = Provisioning.getInstance();

        SetPasswordRequest req = zsc.elementToJaxb(request);
        String id = req.getId();
        String newPassword = req.getNewPassword();

        Account account = prov.get(AccountBy.id, id, zsc.getAuthToken());
        if (account == null) {
            try {
                defendAgainstAccountOrCalendarResourceHarvestingWhenAbsent(AccountBy.id, id, zsc,
                        Admin.R_setAccountPassword, Admin.R_setCalendarResourcePassword);
            } catch (ServiceException se1) {
                defendAgainstAccountOrCalendarResourceHarvestingWhenAbsent(AccountBy.id, id, zsc,
                        Admin.R_changeAccountPassword, Admin.R_changeCalendarResourcePassword);
            }
        } else {
            try {
                defendAgainstAccountOrCalendarResourceHarvesting(account, AccountBy.id, id, zsc,
                        Admin.R_setAccountPassword, Admin.R_setCalendarResourcePassword);
            } catch (ServiceException se1) {
                defendAgainstAccountOrCalendarResourceHarvesting(account, AccountBy.id, id, zsc,
                        Admin.R_changeAccountPassword, Admin.R_changeCalendarResourcePassword);
            }
            boolean enforcePasswordPolicy;
            if (account.isCalendarResource()) {
                CalendarResource resource = prov.get(CalendarResourceBy.id, id);
                enforcePasswordPolicy = checkCalendarResourceRights(zsc, resource);
            } else {
                enforcePasswordPolicy = checkAccountRights(zsc, account);
            }

            SetPasswordResult result = prov.setPassword(account, newPassword, enforcePasswordPolicy);

            ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                    new String[] {"cmd", "SetPassword","name", account.getName()}));


            if (result.hasMessage()) {
                ZimbraLog.security.info(result.getMessage());
                resp = new SetPasswordResponse(result.getMessage());
            } else {
                resp = new SetPasswordResponse(null);
            }

        }
        return zsc.jaxbToElement(resp);
    }

    /*
     * returns whether password strength policies should be enforced for the authed user
     *
     * returns false if user can setAccountPassword
     * returns true if user cannot setAccountPassword but can changeAccountPassword
     *
     * throws PERM_DENIED if user doesn't have either right
     */
    private boolean checkAccountRights(ZimbraSoapContext zsc, Account acct)
            throws ServiceException {
        try {
            checkAccountRight(zsc, acct, Admin.R_setAccountPassword);
            return false;
        } catch (ServiceException e) {
            if (ServiceException.PERM_DENIED.equals(e.getCode())) {
                checkAccountRight(zsc, acct, Admin.R_changeAccountPassword);
                return true;
            } else {
                throw e;
            }
        }
    }

    /*
     * returns whether password strength policies should be enforced for the authed user
     *
     * returns false if user can setCalendarResourcePassword
     * returns true if user cannot setCalendarResourcePassword but can changeCalendarResourcePassword
     *
     * throws PERM_DENIED if user doesn't have either right
     */
    private boolean checkCalendarResourceRights(ZimbraSoapContext zsc, CalendarResource cr)
            throws ServiceException {
        try {
            checkCalendarResourceRight(zsc, cr, Admin.R_setCalendarResourcePassword);
            return false;
        } catch (ServiceException e) {
            if (ServiceException.PERM_DENIED.equals(e.getCode())) {
                checkCalendarResourceRight(zsc, cr, Admin.R_changeCalendarResourcePassword);
                return true;
            } else {
                throw e;
            }
        }
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_setAccountPassword);
        relatedRights.add(Admin.R_changeAccountPassword);
        relatedRights.add(Admin.R_setCalendarResourcePassword);
        relatedRights.add(Admin.R_changeCalendarResourcePassword);
    }
}

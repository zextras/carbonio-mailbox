// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.LockoutMailboxRequest;
import com.zimbra.soap.type.AccountNameSelector;

public class LockoutMailbox extends AdminDocumentHandler {

    protected void checkRights(ZimbraSoapContext lc, Map<String, Object> context, Account account)
    throws ServiceException {
        Provisioning prov = Provisioning.getInstance();
        Server localServer = prov.getLocalServer();
        checkRight(lc, context, localServer, Admin.R_moveMailboxFromServer);
    }

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = super.getZimbraSoapContext(context);
        LockoutMailboxRequest req = zsc.elementToJaxb(request);
        AccountNameSelector acctSel = req.getAccount();
        if (acctSel == null) {
            throw ServiceException.INVALID_REQUEST(String.format("missing <%s>", AdminConstants.E_ACCOUNT), null);
        }

        String accountSelectorKey = acctSel.getKey();
        AccountBy by = acctSel.getBy().toKeyAccountBy();
        Provisioning prov = Provisioning.getInstance();
        Account account = prov.get(by, accountSelectorKey, zsc.getAuthToken());
        defendAgainstAccountOrCalendarResourceHarvesting(account, by, accountSelectorKey, zsc,
                Admin.R_moveAccountMailbox, Admin.R_moveCalendarResourceMailbox);

        // Reload account to pick up any changes made by other servers.
        prov.reload(account);

        // check rights
        checkRights(zsc, context, account);

        String method = request.getAttribute(AdminConstants.A_OPERATION, AdminConstants.A_START);

        if (method.equalsIgnoreCase(AdminConstants.A_START)) {
            MailboxManager.getInstance().lockoutMailbox(account.getId());
        } else if (method.equalsIgnoreCase(AdminConstants.A_END)) {
            MailboxManager.getInstance().undoLockout(account.getId());
        } else {
            throw ServiceException.FAILURE("Unknown lockout method " + method, null);
        }

        return zsc.createElement(AdminConstants.LOCKOUT_MAILBOX_RESPONSE);
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_moveAccountMailbox);
        relatedRights.add(Admin.R_moveCalendarResourceMailbox);
        relatedRights.add(Admin.R_moveMailboxFromServer);

        notes.add("If the account is a calendar resource, need " + Admin.R_moveCalendarResourceMailbox.getName()
                + " right on the calendar resource.");
        notes.add("If the account is a regular account, need " + Admin.R_moveAccountMailbox.getName()
                + " right on the account.");
        notes.add("Need " + Admin.R_moveMailboxFromServer.getName() + " right on the server");
    }
}

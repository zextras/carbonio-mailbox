// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Mar 9, 2005
 */
package com.zimbra.cs.service.admin;

import java.util.List;
import java.util.Map;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.GetMailboxRequest;
import com.zimbra.soap.admin.message.GetMailboxResponse;
import com.zimbra.soap.admin.type.MailboxWithMailboxId;

/**
 * @author dkarp
 */
public class GetMailbox extends AdminDocumentHandler {

    private static final String[] TARGET_ACCOUNT_PATH = new String[] { AdminConstants.E_MAILBOX, AdminConstants.A_ACCOUNTID };
    @Override
    protected String[] getProxiedAccountPath()  { return TARGET_ACCOUNT_PATH; }

    /**
     * must be careful and only allow access to domain if domain admin
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
        ZimbraSoapContext zsc = getZimbraSoapContext(context);

        GetMailboxRequest req = zsc.elementToJaxb(request);
        String accountId = req.getMbox().getId();

        Account account = Provisioning.getInstance().get(AccountBy.id, accountId);
        defendAgainstAccountHarvesting(account, AccountBy.id, accountId, zsc, Admin.R_getMailboxInfo);

        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
        GetMailboxResponse resp = new GetMailboxResponse(
                new MailboxWithMailboxId(mbox.getId(), null, mbox.getSize()));
        return zsc.jaxbToElement(resp);
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        relatedRights.add(Admin.R_getMailboxInfo);
    }
}

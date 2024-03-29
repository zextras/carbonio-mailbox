// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Apr 2, 2005
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.PurgeMessagesResponse;
import com.zimbra.soap.admin.type.MailboxWithMailboxId;
import java.util.List;
import java.util.Map;

/**
 * @author dkarp
 */
public class PurgeMessages extends AdminDocumentHandler {

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    Element mreq = request.getOptionalElement(AdminConstants.E_MAILBOX);
    String[] accounts;
    if (mreq != null) {
      accounts = new String[] {mreq.getAttribute(AdminConstants.A_ACCOUNTID)};

      // accounts are specified, check right or each account
      Provisioning prov = Provisioning.getInstance();
      for (String acctId : accounts) {
        Account acct = prov.get(AccountBy.id, acctId);
        if (acct == null) throw AccountServiceException.NO_SUCH_ACCOUNT(acctId);
        checkAccountRight(zsc, acct, Admin.R_purgeMessages);
      }

    } else {
      // all accounts on the system, has to be a system admin
      checkRight(zsc, context, null, AdminRight.PR_SYSTEM_ADMIN_ONLY);

      accounts = MailboxManager.getInstance().getAccountIds();
    }

    PurgeMessagesResponse purgeResponse = new PurgeMessagesResponse();
    for (String s : accounts) {
      Account account = Provisioning.getInstance().getAccountById(s);
      if (account == null)
        continue;
      MailboxWithMailboxId mboxResp;
      if (Provisioning.getInstance().onLocalServer(account)) { // local
        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account, false);
        if (mbox == null)
          continue;
        mbox.purgeMessages(null);
        mboxResp =
            new MailboxWithMailboxId(mbox.getId(), account.getId(), mbox.getSize());
      } else { // remote
        Server server = account.getServer();
        if (server == null)
          continue;
        SoapProvisioning soapProvisioning = SoapProvisioning.getAdminInstance();
        mboxResp = soapProvisioning.purgeMessages(account);
        if (mboxResp == null)
          continue;
        mboxResp.setAccountId(account.getId());
      }
      purgeResponse.addMailbox(mboxResp);
    }
    return zsc.jaxbToElement(purgeResponse);
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_purgeMessages);
    notes.add(
        "If account ids are specified, needs effective "
            + Admin.R_purgeMessages.getName()
            + " right for each account.  "
            + "If account ids are not specified, the authed account has to be a system admin.");
  }
}

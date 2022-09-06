// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.RenameAccountRequest;
import java.util.List;
import java.util.Map;

/**
 * @author schemers
 */
public class RenameAccount extends AdminDocumentHandler {

  private static final String[] TARGET_ACCOUNT_PATH = new String[] {AdminConstants.E_ID};

  @Override
  protected String[] getProxiedAccountPath() {
    return TARGET_ACCOUNT_PATH;
  }

  /** must be careful and only allow renames from/to domains a domain admin can see */
  @Override
  public boolean domainAuthSufficient(Map<String, Object> context) {
    return true;
  }

  /**
   * @return true - which means accept responsibility for measures to prevent account harvesting by
   *     delegate admins
   */
  @Override
  public boolean defendsAgainstDelegateAdminAccountHarvesting() {
    return true;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    RenameAccountRequest req = zsc.elementToJaxb(request);
    String id = req.getId();
    String newName = req.getNewName();

    Account account = prov.get(AccountBy.id, id, zsc.getAuthToken());
    defendAgainstAccountHarvesting(account, AccountBy.id, id, zsc, Admin.R_renameAccount);

    String oldName = account.getName();

    // check if the admin can rename the account
    checkAccountRight(zsc, account, Admin.R_renameAccount);

    // check if the admin can "create account" in the domain (can be same or diff)
    checkDomainRightByEmail(zsc, newName, Admin.R_createAccount);

    Mailbox mbox =
        Provisioning.onLocalServer(account)
            ? MailboxManager.getInstance().getMailboxByAccount(account)
            : null;
    prov.renameAccount(id, newName);
    if (mbox != null) {
      mbox.renameMailbox(oldName, newName);
    }

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {"cmd", "RenameAccount", "name", oldName, "newName", newName}));

    // get again with new name...
    account = prov.get(AccountBy.id, id, true, zsc.getAuthToken());
    if (account == null) {
      throw ServiceException.FAILURE("unable to get account after rename: " + id, null);
    }
    Element response = zsc.createElement(AdminConstants.RENAME_ACCOUNT_RESPONSE);
    ToXML.encodeAccount(response, account);
    return response;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_renameAccount);
    relatedRights.add(Admin.R_createAccount);
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.events.services.mailbox.UserDeleted;
import com.zextras.mailbox.account.usecase.DeleteUserUseCase;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.DeleteAccountRequest;
import com.zimbra.soap.admin.message.DeleteAccountResponse;

import io.vavr.control.Try;
import java.util.List;
import java.util.Map;

/**
 * @author schemers
 */
public class DeleteAccount extends AdminDocumentHandler {

  private static final String[] TARGET_ACCOUNT_PATH = new String[] {AdminConstants.E_ID};

  private final DeleteUserUseCase deleteUserUseCase;
  private final Try<MessageBrokerClient> messageBrokerClientTry;

  public DeleteAccount(DeleteUserUseCase deleteUserUseCase, Try<MessageBrokerClient> messageBrokerClientTry) {
    this.deleteUserUseCase = deleteUserUseCase;
    this.messageBrokerClientTry = messageBrokerClientTry;
  }

  @Override
  protected String[] getProxiedAccountPath() {
    return TARGET_ACCOUNT_PATH;
  }

  /** must be careful and only allow deletes domain admin has access to */
  @Override
  public boolean domainAuthSufficient(Map context) {
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

  /** Deletes an account and its mailbox. */
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    DeleteAccountRequest req = zsc.elementToJaxb(request);
    String id = req.getId();
    if (null == id) {
      throw ServiceException.INVALID_REQUEST(
          "missing required attribute: " + AdminConstants.E_ID, null);
    }

    // Confirm that the account exists and that the mailbox is located on the current host
    Account account = prov.get(AccountBy.id, id, zsc.getAuthToken());
    defendAgainstAccountHarvesting(account, AccountBy.id, id, zsc, Admin.R_deleteAccount);

    /*
     * bug 69009
     *
     * We delete the mailbox before deleting the LDAP entry.
     * It's possible that a message delivery or other user action could
     * cause the mailbox to be recreated between the mailbox delete step
     * and the LDAP delete step.
     *
     * To prevent this race condition, put the account in "maintenance" mode
     * so mail delivery and any user action is blocked.
     */
    deleteUserUseCase.delete(account.getId()).getOrElseThrow(ex -> ServiceException.FAILURE("Delete account "
            + account.getMail() + " has an error: "
            + ex.getMessage(), ex));
    publishAccountDeletedEvent(account);

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {
              "cmd", "DeleteAccount", "name", account.getName(), "id", account.getId()
            }));

    return zsc.jaxbToElement(new DeleteAccountResponse());
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_deleteAccount);
  }

  private void publishAccountDeletedEvent(Account account) {
    String userId = account.getId();
    try {
			final MessageBrokerClient messageBrokerClient = messageBrokerClientTry.get();
			boolean result = messageBrokerClient.publish(new UserDeleted(userId));
      if (result) {
        ZimbraLog.account.info("Published deleted account event for user: " + userId);
      } else {
        ZimbraLog.account.error("Failed to publish deleted account event for user: " + userId);
      }
    } catch (Exception e){
      ZimbraLog.account.error("Exception while publishing deleted account event for user: " + userId, e);
    }
  }
}

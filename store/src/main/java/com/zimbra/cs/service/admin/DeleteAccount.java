// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Jun 17, 2004
 */
package com.zimbra.cs.service.admin;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.carbonio.message_broker.events.services.mailbox.DeleteUserRequested;
import com.zextras.mailbox.account.usecase.DeleteUserUseCase;
import com.zextras.mailbox.client.ServiceInstalledProvider;
import com.zextras.mailbox.messageBroker.MessageBrokerFactory;
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
import java.util.List;
import java.util.Map;

/**
 * @author schemers
 */
public class DeleteAccount extends AdminDocumentHandler {

  private static final String[] TARGET_ACCOUNT_PATH = new String[] {AdminConstants.E_ID};

  private final DeleteUserUseCase deleteUserUseCase;
  private final ServiceInstalledProvider filesInstalledProvider;

  public DeleteAccount(DeleteUserUseCase deleteUserUseCase, ServiceInstalledProvider filesInstalledProvider) {
    this.deleteUserUseCase = deleteUserUseCase;
    this.filesInstalledProvider = filesInstalledProvider;
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

    // If files is installed, mailbox must emit an event so files will delete user's files and blobs and only then
    // send another event back to mailbox to delete the account (see DeletedUserFilesConsumer)
    // If files is not installed, mailbox can delete the account directly as it always did
    boolean isFilesInstalled;
		try {
			isFilesInstalled = filesInstalledProvider.isInstalled();
		} catch (Exception e) {
			throw ServiceException.FAILURE("Delete account " + account.getMail() + " has an error: Unable to check if files is installed", e);
		}

    if (isFilesInstalled) {
      // Since Files is installed, this will throw an exception if it fails to publish the event, both if message broker
      // is down or if it is not installed, to avoid deleting the account without deleting the files.
      boolean success = publishDeleteUserRequestedEvent(account);
      if (!success) {
        throw ServiceException.FAILURE("Delete account " + account.getMail() + " has an error: Failed to publish delete user requested event", null);
      }
    } else {
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

      ZimbraLog.security.info(
          ZimbraLog.encodeAttrs(
              new String[] {
                "cmd", "DeleteAccount", "name", account.getName(), "id", account.getId()
              }));
    }

    return zsc.jaxbToElement(new DeleteAccountResponse());
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_deleteAccount);
  }

  private boolean publishDeleteUserRequestedEvent(Account account) {
    String userId = account.getId();
    try {
      MessageBrokerClient messageBrokerClient = MessageBrokerFactory.getMessageBrokerClientInstance();
			boolean result = messageBrokerClient.publish(new DeleteUserRequested(userId));
      if (result) {
        ZimbraLog.account.info("Published deleted account event for user: " + userId);
      } else {
        ZimbraLog.account.error("Failed to publish deleted account event for user: " + userId);
      }
      return result;
    } catch (Exception e){
      ZimbraLog.account.error("Exception while publishing deleted account event for user: " + userId, e);
      return false;
    }
  }
}

// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.messageBroker.consumers;

import com.zextras.carbonio.message_broker.config.EventConfig;
import com.zextras.carbonio.message_broker.consumer.BaseConsumer;
import com.zextras.carbonio.message_broker.events.generic.BaseEvent;
import com.zextras.carbonio.message_broker.events.services.files.DeletedUserFiles;
import com.zextras.mailbox.account.usecase.DeleteUserUseCase;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeletedUserFilesConsumer extends BaseConsumer {

  private static final Logger logger = LoggerFactory.getLogger(DeletedUserFilesConsumer.class);

  private final DeleteUserUseCase deleteUserUseCase;

  public DeletedUserFilesConsumer(DeleteUserUseCase deleteUserUseCase) {
    this.deleteUserUseCase = deleteUserUseCase;
  }

  @Override
  protected EventConfig getEventConfig() {
    return EventConfig.DELETED_USER_FILES;
  }

  @Override
  public void doHandle(BaseEvent baseMessageBrokerEvent) {
    DeletedUserFiles deletedUserFiles = (DeletedUserFiles) baseMessageBrokerEvent;
    logger.info("Received DeletedUserFiles({})", deletedUserFiles.getUserId());

    // Delete account from Mailbox.
    // Here, the user's files and blobs have already been deleted, so it's safe to delete the account.

    // Code from DeleteAccount
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
    try {
      deleteUserUseCase.delete(deletedUserFiles.getUserId()).getOrElseThrow(ex -> ServiceException.FAILURE("Delete account "
              + deletedUserFiles.getUserId() + " has an error: "
              + ex.getMessage(), ex));
    } catch (ServiceException e) {
      // Launch runtime exception to avoid sending ack to the message broker when an error occurs, the event will be reprocessed
      throw new RuntimeException(e);
    }

    ZimbraLog.security.info(
        ZimbraLog.encodeAttrs(
            new String[] {
              "cmd", "DeleteAccount", "id", deletedUserFiles.getUserId()
            }));

    ZimbraLog.security.info(
        "DELETE_OPERATION user deleted for real from the consumer");
  }
}

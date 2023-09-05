// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.usecase;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;

public class EmptyFolderActionUseCase {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public EmptyFolderActionUseCase(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method doesn't recursively remove the folders, it just empties the first one. Have a look
   * at {@link #emptyRecursively} for recursively emptying a folder.
   *
   * @param accountId the target account which mailbox folder will be emptied
   * @param folderId the id of the folder (belonging to the accountId) that will be emptied
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> empty(OperationContext operationContext, String accountId, String folderId) {
    return innerEmptyCall(operationContext, accountId, folderId, false);
  }

  /**
   * This method does recursively remove the folders.
   *
   * @param accountId the target account which mailbox folder will be emptied
   * @param folderId the id of the folder (belonging to the accountId) that will be emptied
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> emptyRecursively(
      OperationContext operationContext, String accountId, String folderId) {
    return innerEmptyCall(operationContext, accountId, folderId, true);
  }

  private Try<Void> innerEmptyCall(
      OperationContext operationContext,
      String accountId,
      String folderId,
      boolean removeSubFolders) {
    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          final ItemId itemId = itemIdFactory.create(folderId, accountId);
          userMailbox.emptyFolder(operationContext, itemId.getId(), removeSubFolders);

          if (itemId.getId() == Mailbox.ID_FOLDER_TRASH) {
            userMailbox.purgeImapDeleted(operationContext);
          }
        });
  }
}
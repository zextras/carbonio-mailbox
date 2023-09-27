// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.usecase.folderaction;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import javax.inject.Inject;

/**
 * Use case class to empty a {@link com.zimbra.cs.mailbox.Folder}.
 *
 * @author Davide Polonio
 * @since 23.10.0
 */
public class EmptyFolderAction {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public EmptyFolderAction(final MailboxManager mailboxManager, final ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method doesn't recursively remove the folders, it just empties the first one. Have a look
   * at {@link #emptyRecursively} for recursively emptying a folder.
   *
   * @param accountId the target account which mailbox folder will be emptied
   * @param folderId the id of the folder (belonging to the accountId)
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> empty(
      final OperationContext operationContext, final String accountId, final String folderId) {
    return innerEmptyCall(operationContext, accountId, folderId, false);
  }

  /**
   * This method does recursively remove the folders.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account which mailbox folder will be emptied
   * @param folderId the id of the folder (belonging to the accountId)
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> emptyRecursively(
      final OperationContext operationContext, final String accountId, final String folderId) {
    return innerEmptyCall(operationContext, accountId, folderId, true);
  }

  private Try<Void> innerEmptyCall(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final boolean removeSubFolders) {
    return mailboxManager
        .tryGetMailboxByAccountId(accountId, true)
        .flatMap(
            userMailbox ->
                Try.run(
                    () -> {
                      final ItemId itemId = itemIdFactory.create(folderId, accountId);
                      userMailbox.emptyFolder(operationContext, itemId.getId(), removeSubFolders);
                      if (itemId.getId() == Mailbox.ID_FOLDER_TRASH) {
                        userMailbox.purgeImapDeleted(operationContext);
                      }
                    }));
  }
}

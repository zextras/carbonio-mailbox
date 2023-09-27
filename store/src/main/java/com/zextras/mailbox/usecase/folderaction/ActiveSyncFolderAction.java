package com.zextras.mailbox.usecase.folderaction;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import javax.inject.Inject;

/**
 * Use case class to manage ActiveSync support on a {@link com.zimbra.cs.mailbox.Folder}.
 *
 * @author Yuliya Aheeva
 * @since 23.10.0
 */
public class ActiveSyncFolderAction {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public ActiveSyncFolderAction(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to enable ActiveSync on a folder.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account owner of the mailbox
   * @param folderId the id of the folder (belonging to the accountId)
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> enableActiveSync(
      final OperationContext operationContext, final String accountId, final String folderId) {
    return innerActiveSyncCall(operationContext, accountId, folderId, false);
  }

  /**
   * This method is used to disable ActiveSync on a folder.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account which mailbox folder will be emptied
   * @param folderId the id of the folder (belonging to the accountId) that will be emptied
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> disableActiveSync(
      final OperationContext operationContext, final String accountId, final String folderId) {
    return innerActiveSyncCall(operationContext, accountId, folderId, true);
  }

  private Try<Void> innerActiveSyncCall(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final boolean disableActiveSyncFlag) {

    return mailboxManager
        .tryGetMailboxByAccountId(accountId, true)
        .flatMap(
            userMailbox ->
                Try.run(
                    () -> {
                      final ItemId itemId = itemIdFactory.create(folderId, accountId);
                      userMailbox.setActiveSyncDisabled(
                          operationContext, itemId.getId(), disableActiveSyncFlag);
                    }));
  }
}

package com.zextras.mailbox.usecase;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Use case class to manage ActiveSync support on a folder.
 *
 * @author Yuliya Aheeva
 * @since 23.10.0
 */
public class ActiveSyncFolderActionUseCase {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public ActiveSyncFolderActionUseCase(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to enable ActiveSync on a folder.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
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

    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          final ItemId itemId = itemIdFactory.create(folderId, accountId);

          userMailbox.setActiveSyncDisabled(
              operationContext, itemId.getId(), disableActiveSyncFlag);
        });
  }
}

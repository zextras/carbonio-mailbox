package com.zextras.mailbox.usecase;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Use case class to manage synchronization on a folder.
 *
 * @author Yuliya Aheeva
 * @since 23.10.0
 */
public class SyncFolderActionUseCase {
  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  private static final boolean ENABLE_SYNC = true;
  private static final boolean DISABLE_SYNC = false;

  @Inject
  public SyncFolderActionUseCase(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to turn on the synchronization.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> syncOn(
      final OperationContext operationContext, final String accountId, final String folderId) {
    return innerSyncCall(operationContext, accountId, folderId, ENABLE_SYNC);
  }

  /**
   * This method is used to turn off the synchronization.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> syncOff(
      final OperationContext operationContext, final String accountId, final String folderId) {
    return innerSyncCall(operationContext, accountId, folderId, DISABLE_SYNC);
  }

  private Try<Void> innerSyncCall(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final boolean syncFlag) {

    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          final ItemId itemId = itemIdFactory.create(folderId, accountId);

          userMailbox.alterTag(
              operationContext,
              itemId.getId(),
              MailItem.Type.FOLDER,
              Flag.FlagInfo.SYNC,
              syncFlag,
              null);
        });
  }
}

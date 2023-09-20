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
 * Use case class to set web offline sync days.
 *
 * @author Dima Dymkovets
 * @since 23.10.0
 */
public class SetWebOfflineSyncDaysFolderActionUseCase {
  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public SetWebOfflineSyncDaysFolderActionUseCase(
      MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to set web offline sync days.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param days number of days
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> setWebOfflineSyncDays(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final int days) {
    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          final ItemId itemId = itemIdFactory.create(folderId, accountId);

          userMailbox.setFolderWebOfflineSyncDays(operationContext, itemId.getId(), days);
        });
  }
}

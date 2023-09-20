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
 * Use case class to refresh a folder.
 *
 * @author Dima Dymkovets
 * @since 23.10.0
 */
public class RefreshFolderActionUseCase {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public RefreshFolderActionUseCase(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to refresh (synchronize data) a folder.
   *
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param url the url to retrieve feed from
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> refresh(
      final OperationContext operationContext, final String accountId, final String folderId) {
    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          final ItemId itemId = itemIdFactory.create(folderId, accountId);

          userMailbox.synchronizeFolder(operationContext, itemId.getId());
        });
  }
}

package com.zextras.mailbox.usecase.folderaction;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import javax.inject.Inject;

/**
 * Use case class to refresh a {@link com.zimbra.cs.mailbox.Folder}.
 *
 * @author Dima Dymkovets
 * @since 23.10.0
 */
public class RefreshFolderAction {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public RefreshFolderAction(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
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
    return mailboxManager
        .tryGetMailboxByAccountId(accountId, true)
        .flatMap(
            userMailbox ->
                Try.run(
                    () -> {
                      final ItemId itemId = itemIdFactory.create(folderId, accountId);

                      userMailbox.synchronizeFolder(operationContext, itemId.getId());
                    }));
  }
}

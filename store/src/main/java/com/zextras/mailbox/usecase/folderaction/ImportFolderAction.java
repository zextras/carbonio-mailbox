package com.zextras.mailbox.usecase.folderaction;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import javax.inject.Inject;

/**
 * Use case class to import feed from remote datasource to a {@link com.zimbra.cs.mailbox.Folder}.
 *
 * @author Yuliya Aheeva
 * @since 23.10.0
 */
public class ImportFolderAction {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public ImportFolderAction(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to import feed from remote datasource.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param url the url to retrieve feed from
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> importFeed(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final String url) {
    return mailboxManager
        .tryGetMailboxByAccountId(accountId, true)
        .flatMap(
            userMailbox ->
                Try.run(
                    () -> {
                      final ItemId itemId = itemIdFactory.create(folderId, accountId);
                      userMailbox.importFeed(operationContext, itemId.getId(), url, false);
                    }));
  }
}

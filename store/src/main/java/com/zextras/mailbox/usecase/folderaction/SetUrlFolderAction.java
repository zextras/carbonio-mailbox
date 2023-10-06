package com.zextras.mailbox.usecase.folderaction;

import com.google.common.base.Strings;
import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import javax.inject.Inject;

/**
 * Use case class to set folder url on a {@link com.zimbra.cs.mailbox.Folder}.
 *
 * @author Dima Dymkovets
 * @since 23.10.0
 */
public class SetUrlFolderAction {
  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public SetUrlFolderAction(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to set folder url.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param url url
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> setFolderUrl(
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

                      userMailbox.setFolderUrl(operationContext, itemId.getId(), url);

                      if (Strings.isNullOrEmpty(url)) {
                        userMailbox.synchronizeFolder(operationContext, itemId.getId());
                      }
                    }));
  }
}

package com.zextras.mailbox.usecase.folderaction;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import javax.inject.Inject;

/**
 * Use case class to set checked folder tag.
 *
 * @author Yuliya Aheeva
 * @since 23.10.0
 */
public class CheckFolderAction {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public CheckFolderAction(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to tag folder as checked.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> check(
      final OperationContext operationContext, final String accountId, final String folderId) {
    return innerCheckCall(operationContext, accountId, folderId, true);
  }

  /**
   * This method is used to tag folder as unchecked.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> uncheck(
      final OperationContext operationContext, final String accountId, final String folderId) {
    return innerCheckCall(operationContext, accountId, folderId, false);
  }

  private Try<Void> innerCheckCall(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final boolean checkFlag) {

    return mailboxManager
        .tryGetMailboxByAccountId(accountId, true)
        .flatMap(
            userMailbox ->
                Try.run(
                    () -> {
                      final ItemId itemId = itemIdFactory.create(folderId, accountId);

                      userMailbox.alterTag(
                          operationContext,
                          itemId.getId(),
                          MailItem.Type.FOLDER,
                          Flag.FlagInfo.CHECKED,
                          checkFlag,
                          null);
                    }));
  }
}

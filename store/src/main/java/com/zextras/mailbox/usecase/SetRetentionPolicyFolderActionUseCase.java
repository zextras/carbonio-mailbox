package com.zextras.mailbox.usecase;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.soap.mail.type.RetentionPolicy;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Use case class to set retention policy on a folder.
 *
 * @author Dima Dymkovets
 * @since 23.10.0
 */
public class SetRetentionPolicyFolderActionUseCase {
  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public SetRetentionPolicyFolderActionUseCase(
      MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  /**
   * This method is used to set retention policy on a folder.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param retentionPolicy {@link RetentionPolicy}
   * @return a {@link Try} object with the status of the operation
   */
  public Try<Void> setRetentionPolicy(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final RetentionPolicy retentionPolicy) {
    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          final ItemId itemId = itemIdFactory.create(folderId, accountId);

          userMailbox.setRetentionPolicy(
              operationContext, itemId.getId(), MailItem.Type.FOLDER, retentionPolicy);
        });
  }
}

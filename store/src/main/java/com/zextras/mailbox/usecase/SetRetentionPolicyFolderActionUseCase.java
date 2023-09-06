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

public class SetRetentionPolicyFolderActionUseCase {
  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public SetRetentionPolicyFolderActionUseCase(
      MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  public Try<Void> setRetentionPolicy(
      OperationContext operationContext,
      String accountId,
      String folderId,
      RetentionPolicy retentionPolicy) {
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

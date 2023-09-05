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

public class CheckFolderActionUseCase {

  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public CheckFolderActionUseCase(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  public Try<Void> check(OperationContext operationContext, String accountId, String folderId) {
    return innerCheckCall(operationContext, accountId, folderId, true);
  }

  public Try<Void> uncheck(OperationContext operationContext, String accountId, String folderId) {
    return innerCheckCall(operationContext, accountId, folderId, false);
  }

  private Try<Void> innerCheckCall(
      OperationContext operationContext, String accountId, String folderId, boolean checkFlag) {

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
              Flag.FlagInfo.CHECKED,
              checkFlag,
              null);
        });
  }
}

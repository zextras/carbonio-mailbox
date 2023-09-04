package com.zextras.mailbox.usecase;

import com.google.common.base.Strings;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;

public class SetUrlFolderActionUseCase {
  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public SetUrlFolderActionUseCase(MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  public Try<Void> setFolderUrl(
      OperationContext operationContext,
      String accountId,
      String folderId,
      String url,
      boolean excludeFreeBusy) {
    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          final ItemId itemId = itemIdFactory.create(folderId, accountId);
          userMailbox.setFolderUrl(operationContext, itemId.getId(), url);
          if (Strings.isNullOrEmpty(url)) {
            userMailbox.synchronizeFolder(operationContext, itemId.getId());
            return;
          }
          userMailbox.alterTag(
              operationContext,
              itemId.getId(),
              MailItem.Type.FOLDER,
              Flag.FlagInfo.EXCLUDE_FREEBUSY,
              excludeFreeBusy,
              null);
        });
  }
}

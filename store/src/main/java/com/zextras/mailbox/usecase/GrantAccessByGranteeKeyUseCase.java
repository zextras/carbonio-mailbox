package com.zextras.mailbox.usecase;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;

public class GrantAccessByGranteeKeyUseCase {
  private final MailboxManager mailboxManager;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public GrantAccessByGranteeKeyUseCase(
      MailboxManager mailboxManager, ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemIdFactory = itemIdFactory;
  }

  public Try<Void> grantAccess(
      OperationContext operationContext,
      String accountId,
      String folderId,
      String display,
      String accessKey,
      String rights,
      long expiry) {
    return Try.run(
        () -> {
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId, true))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "unable to locate the mailbox for the given accountId"));

          final ItemId itemId = itemIdFactory.create(folderId, accountId);
          userMailbox.grantAccess(
              operationContext,
              itemId.getId(),
              display,
              ACL.GRANTEE_KEY,
              ACL.stringToRights(rights),
              accessKey,
              expiry);
        });
  }
}

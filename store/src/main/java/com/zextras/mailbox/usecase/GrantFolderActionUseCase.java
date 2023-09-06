package com.zextras.mailbox.usecase;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.mail.ItemActionUtil;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.util.AccountUtil;
import io.vavr.control.Try;
import java.util.Optional;
import javax.inject.Inject;

public class GrantFolderActionUseCase {

  private final MailboxManager mailboxManager;
  private final ItemActionUtil itemActionUtil;
  private final AccountUtil accountUtil;
  private final ItemIdFactory itemIdFactory;

  @Inject
  public GrantFolderActionUseCase(
      MailboxManager mailboxManager,
      ItemActionUtil itemActionUtil,
      AccountUtil accountUtil,
      ItemIdFactory itemIdFactory) {
    this.mailboxManager = mailboxManager;
    this.itemActionUtil = itemActionUtil;
    this.accountUtil = accountUtil;
    this.itemIdFactory = itemIdFactory;
  }

  public static class Result {
    private final NamedEntry namedEntry;
    private final ACL.Grant grant;

    private final String zimbraId;

    public Result(NamedEntry namedEntry, ACL.Grant grant, String zimbraId) {
      this.namedEntry = namedEntry;
      this.grant = grant;
      this.zimbraId = zimbraId;
    }

    public NamedEntry getNamedEntry() {
      return namedEntry;
    }

    public ACL.Grant getGrant() {
      return grant;
    }

    public String getZimbraId() {
      return zimbraId;
    }
  }

  public Try<Result> grant(
      byte grantType,
      String zimbraId,
      long expiry,
      String grantExpiry,
      String accountId,
      OperationContext operationContext,
      String folderId) {
    return Try.of(
        () -> {
          String calculatedZimbraId = zimbraId;
          long calculatedExpiration = expiry;
          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "Unable to retrieve a mailbox for this accountId"));
          final ItemId itemId = itemIdFactory.create(folderId, accountId);
          switch (grantType) {
            case ACL.GRANTEE_AUTHUSER:
              {
                calculatedZimbraId = GuestAccount.GUID_AUTHUSER;
                break;
              }
            case ACL.GRANTEE_PUBLIC:
              {
                calculatedZimbraId = GuestAccount.GUID_PUBLIC;
                calculatedExpiration =
                    itemActionUtil.validateGrantExpiry(
                        grantExpiry,
                        accountUtil.getMaxPublicShareLifetime(
                            userMailbox.getAccount(),
                            userMailbox
                                .getFolderById(operationContext, itemId.getId())
                                .getDefaultView()));
                break;
              }
          }

          final ACL.Grant grantResult =
              userMailbox.grantAccess(
                  operationContext,
                  itemId.getId(),
                  calculatedZimbraId,
                  grantType,
                  (short) 0,
                  null,
                  calculatedExpiration);

          return new Result(null, grantResult, calculatedZimbraId);
        });
  }
}

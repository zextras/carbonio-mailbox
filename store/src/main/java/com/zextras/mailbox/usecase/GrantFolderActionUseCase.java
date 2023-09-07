package com.zextras.mailbox.usecase;

import com.google.common.collect.Sets;
import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.ldap.GranteeProvider;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.account.MailTarget;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.ACL.Grant;
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
  private final GranteeProvider granteeProvider;

  @Inject
  public GrantFolderActionUseCase(
      MailboxManager mailboxManager,
      ItemActionUtil itemActionUtil,
      AccountUtil accountUtil,
      ItemIdFactory itemIdFactory,
      GranteeProvider granteeProvider) {
    this.mailboxManager = mailboxManager;
    this.itemActionUtil = itemActionUtil;
    this.accountUtil = accountUtil;
    this.itemIdFactory = itemIdFactory;
    this.granteeProvider = granteeProvider;
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
      String folderId,
      String display,
      String secretArgs,
      String secretPassword) {
    return Try.of(
        () -> {
          String calculatedZimbraId = zimbraId;
          long calculatedExpiration = expiry;
          byte calculatedGrantType = grantType;
          String calculatedSecret = null;
          NamedEntry nentry = null;

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
            case ACL.GRANTEE_GUEST:
              {
                calculatedZimbraId = display;
                if (calculatedZimbraId == null || calculatedZimbraId.indexOf('@') < 0)
                  throw ServiceException.INVALID_REQUEST("invalid guest id or password", null);

                // first make sure they didn't accidentally specify "guest" instead of "usr"
                boolean guestGrantee = true;
                try {
                  nentry =
                      granteeProvider.lookupGranteeByName(
                          calculatedZimbraId, ACL.GRANTEE_USER, operationContext);
                  if (nentry instanceof MailTarget) {
                    Domain domain = Provisioning.getInstance().getDomain(userMailbox.getAccount());
                    String granteeDomainName = ((MailTarget) nentry).getDomainName();
                    if (domain.isInternalSharingCrossDomainEnabled()
                        || domain.getName().equals(granteeDomainName)
                        || Sets.newHashSet(domain.getInternalSharingDomain())
                            .contains(granteeDomainName)) {
                      guestGrantee = false;
                      calculatedZimbraId = nentry.getId();
                      calculatedGrantType =
                          nentry instanceof Group ? ACL.GRANTEE_GROUP : ACL.GRANTEE_USER;
                    }
                  }
                } catch (ServiceException e) {
                  // this is the normal path, where lookupGranteeByName throws account.NO_SUCH_USER
                }
                if (guestGrantee) {
                  calculatedSecret = secretArgs;
                  // password is no longer required for external sharing
                  if (calculatedSecret == null) {
                    calculatedSecret = secretPassword;
                  }
                }
                break;
              }
          }

          Grant grantResult =
              grantAccess(
                  calculatedGrantType,
                  operationContext,
                  calculatedSecret,
                  userMailbox,
                  itemId,
                  calculatedZimbraId,
                  calculatedExpiration);

          return new Result(nentry, grantResult, calculatedZimbraId);
        });
  }

  private Grant grantAccess(
      final byte grantType,
      final OperationContext operationContext,
      final String secret,
      final Mailbox userMailbox,
      final ItemId itemId,
      final String calculatedZimbraId,
      final long calculatedExpiration)
      throws ServiceException {

    return userMailbox.grantAccess(
        operationContext,
        itemId.getId(),
        calculatedZimbraId,
        grantType,
        (short) 0,
        secret,
        calculatedExpiration);
  }
}

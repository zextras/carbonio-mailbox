package com.zextras.mailbox.usecase;

import com.google.common.collect.Sets;
import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.ldap.GranteeProvider;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccountServiceException;
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

/**
 * Use case class to grant access to a folder.
 *
 * @author Yuliya Aheeva, Davide Polonio, Dima Dymkovets
 * @since 23.10.0
 */
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

  /**
   * This method is used to grant access on a folder.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account which mailbox folder will be emptied
   * @param folderId the id of the folder (belonging to the accountId) that will be emptied
   * @param granteeType representation of ACL grantee type
   * @param zimbraId folder zimbraId attribute
   * @param expiry expiration time
   * @param display display
   * @param secretArgs secret args
   * @param secretPassword password
   * @param secretAccessKey access key
   * @return a {@link Try} result {@link Result} object with the status of the operation
   */
  public Try<Result> grant(
      OperationContext operationContext,
      String accountId,
      String folderId,
      byte granteeType,
      String zimbraId,
      long expiry,
      String display,
      short rights,
      String secretArgs,
      String secretPassword,
      String secretAccessKey) {
    return Try.of(
        () -> {
          String calculatedZimbraId = zimbraId;
          long calculatedExpiration = expiry;
          byte calculatedGrantType = granteeType;
          String calculatedSecret = null;
          NamedEntry namedEntry = null;

          final Mailbox userMailbox =
              Optional.ofNullable(mailboxManager.getMailboxByAccountId(accountId))
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "Unable to retrieve a mailbox for this accountId"));
          final ItemId itemId = itemIdFactory.create(folderId, accountId);

          switch (granteeType) {
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
                        String.valueOf(expiry),
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
                  namedEntry =
                      granteeProvider.lookupGranteeByName(
                          calculatedZimbraId, ACL.GRANTEE_USER, operationContext);
                  if (namedEntry instanceof MailTarget) {
                    Domain domain = Provisioning.getInstance().getDomain(userMailbox.getAccount());
                    String granteeDomainName = ((MailTarget) namedEntry).getDomainName();
                    if (domain.isInternalSharingCrossDomainEnabled()
                        || domain.getName().equals(granteeDomainName)
                        || Sets.newHashSet(domain.getInternalSharingDomain())
                            .contains(granteeDomainName)) {
                      guestGrantee = false;
                      calculatedZimbraId = namedEntry.getId();
                      calculatedGrantType =
                          namedEntry instanceof Group ? ACL.GRANTEE_GROUP : ACL.GRANTEE_USER;
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
            case ACL.GRANTEE_KEY:
              {
                calculatedZimbraId = display;
                calculatedSecret = secretAccessKey;
                break;
              }
            default:
              {
                if (zimbraId != null) {
                  namedEntry = granteeProvider.lookupGranteeByZimbraId(zimbraId, granteeType);
                } else {
                  try {
                    namedEntry =
                        granteeProvider.lookupGranteeByName(display, granteeType, operationContext);
                    calculatedZimbraId = namedEntry.getId();
                    // make sure they didn't accidentally specify "usr" instead of "grp"
                    if (granteeType == ACL.GRANTEE_USER && namedEntry instanceof Group) {
                      calculatedGrantType = ACL.GRANTEE_GROUP;
                    }
                  } catch (ServiceException e) {
                    if (AccountServiceException.NO_SUCH_ACCOUNT.equals(e.getCode())) {
                      // looks like the case of an internal user not yet provisioned
                      // we'll treat it as external sharing
                      calculatedGrantType = ACL.GRANTEE_GUEST;
                      calculatedZimbraId = display;
                    } else {
                      throw e;
                    }
                  }
                }
              }
          }

          Grant grantResult =
              grantAccess(
                  userMailbox,
                  operationContext,
                  itemId,
                  calculatedZimbraId,
                  calculatedGrantType,
                  rights,
                  calculatedSecret,
                  calculatedExpiration);

          return new Result(namedEntry, grantResult, calculatedZimbraId);
        });
  }

  private Grant grantAccess(
      final Mailbox userMailbox,
      final OperationContext operationContext,
      final ItemId itemId,
      final String calculatedZimbraId,
      final byte grantType,
      final short rights,
      final String secret,
      final long calculatedExpiration)
      throws ServiceException {

    return userMailbox.grantAccess(
        operationContext,
        itemId.getId(),
        calculatedZimbraId,
        grantType,
        rights,
        secret,
        calculatedExpiration);
  }
}

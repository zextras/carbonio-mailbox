package com.zextras.mailbox.usecase.folderaction;

import com.google.common.collect.Sets;
import com.zextras.mailbox.midlewarepojo.GrantInput;
import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.service.GranteeService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.account.MailTarget;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLHelper;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.ACL.Grant;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.util.AccountUtil;
import io.vavr.control.Try;
import javax.inject.Inject;

/**
 * Use case class to grant access on a {@link com.zimbra.cs.mailbox.Folder}.
 *
 * @author Yuliya Aheeva, Davide Polonio, Dima Dymkovets
 * @since 23.10.0
 */
public class GrantFolderAction {

  private final MailboxManager mailboxManager;
  private final AccountUtil accountUtil;
  private final ItemIdFactory itemIdFactory;
  private final GranteeService granteeService;
  private final Provisioning provisioning;

  @Inject
  public GrantFolderAction(
      MailboxManager mailboxManager,
      AccountUtil accountUtil,
      ItemIdFactory itemIdFactory,
      GranteeService granteeService,
      Provisioning provisioning) {
    this.mailboxManager = mailboxManager;
    this.accountUtil = accountUtil;
    this.itemIdFactory = itemIdFactory;
    this.granteeService = granteeService;
    this.provisioning = provisioning;
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
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param grantInput {@link GrantInput}
   * @return a {@link Try} result {@link Result} object with the status of the operation
   */
  public Try<Result> grant(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final GrantInput grantInput) {

    final byte granteeType = grantInput.getGranteeType();
    switch (granteeType) {
      case ACL.GRANTEE_AUTHUSER:
        {
          return grantGranteeAuthUserAccess(
              operationContext,
              accountId,
              folderId,
              grantInput.getRights(),
              grantInput.getGrantExpiry());
        }
      case ACL.GRANTEE_PUBLIC:
        {
          return grantGranteePublicAccess(
              operationContext,
              accountId,
              folderId,
              grantInput.getRights(),
              grantInput.getGrantExpiry());
        }
      case ACL.GRANTEE_GUEST:
        {
          return grantGranteeGuestAccess(
              operationContext,
              accountId,
              folderId,
              grantInput.getDisplay(),
              grantInput.getRights(),
              grantInput.getGrantExpiry(),
              grantInput.getSecretArgs(),
              grantInput.getPassword());
        }
      case ACL.GRANTEE_KEY:
        {
          return grantGranteeKeyAccess(
              operationContext,
              accountId,
              folderId,
              grantInput.getRights(),
              grantInput.getGrantExpiry(),
              grantInput.getDisplay(),
              grantInput.getAccessKey());
        }
      default:
        {
          return grantGranteeDefaultAccess(
              operationContext,
              accountId,
              folderId,
              grantInput.getRights(),
              grantInput.getGrantExpiry(),
              grantInput.getZid(),
              grantInput.getDisplay(),
              granteeType);
        }
    }
  }

  /**
   * This method is used to grant access on a folder when ACL grantee type is not specified.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param rights {@link ACL#stringToRights(String)}}
   * @param expiry expiration time
   * @param zimbraId zimbra id
   * @param displayName display name
   * @param granteeType representation of ACL grantee type
   * @return a {@link Try} result {@link Result} object with the status of the operation
   */
  public Try<Result> grantGranteeDefaultAccess(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final short rights,
      final long expiry,
      final String zimbraId,
      final String displayName,
      final byte granteeType) {
    return mailboxManager
        .tryGetMailboxByAccountId(accountId, true)
        .mapTry(
            userMailbox -> {
              final ItemId itemId = itemIdFactory.create(folderId, accountId);
              NamedEntry namedEntry = null;
              String calculatedZimbraId = zimbraId;
              byte calculatedGranteeType = granteeType;
              if (zimbraId != null) {
                namedEntry = granteeService.lookupGranteeByZimbraId(zimbraId, granteeType);
              } else {
                try {
                  namedEntry =
                      granteeService.lookupGranteeByName(
                          displayName, granteeType, operationContext);
                  calculatedZimbraId = namedEntry.getId();
                  // make sure they didn't accidentally specify "usr" instead of "grp"
                  if (granteeType == ACL.GRANTEE_USER && namedEntry instanceof Group) {
                    calculatedGranteeType = ACL.GRANTEE_GROUP;
                  }
                } catch (ServiceException e) {
                  if (AccountServiceException.NO_SUCH_ACCOUNT.equals(e.getCode())) {
                    // looks like the case of an internal user not yet provisioned
                    // we'll treat it as external sharing
                    calculatedGranteeType = ACL.GRANTEE_GUEST;
                    calculatedZimbraId = displayName;
                  } else {
                    throw e;
                  }
                }
              }
              final Grant grantResult =
                  grantAccess(
                      userMailbox,
                      operationContext,
                      itemId,
                      calculatedZimbraId,
                      calculatedGranteeType,
                      rights,
                      null,
                      expiry);

              return new Result(namedEntry, grantResult, calculatedZimbraId);
            });
  }

  /**
   * This method is used to grant access on a folder when ACL grantee type is {@link
   * ACL.GRANTEE_KEY}.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param rights {@link ACL#stringToRights(String)}}
   * @param expiry expiration time
   * @param displayName display name
   * @param secretAccessKey access key
   * @return a {@link Try} result {@link Result} object with the status of the operation
   */
  public Try<Result> grantGranteeKeyAccess(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final short rights,
      final long expiry,
      final String displayName,
      final String secretAccessKey) {
    return mailboxManager
        .tryGetMailboxByAccountId(accountId, true)
        .mapTry(
            userMailbox -> {
              final ItemId itemId = itemIdFactory.create(folderId, accountId);
              final Grant grantResult =
                  grantAccess(
                      userMailbox,
                      operationContext,
                      itemId,
                      displayName,
                      ACL.GRANTEE_KEY,
                      rights,
                      secretAccessKey,
                      expiry);

              return new Result(null, grantResult, displayName);
            });
  }

  /**
   * This method is used to grant access on a folder when ACL grantee type is {@link
   * ACL.GRANTEE_GUEST}.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param displayName display name
   * @param rights {@link ACL#stringToRights(String)}}
   * @param expiry expiration time
   * @param secretArgs guest secret arguments
   * @param secretPassword guest password
   * @return a {@link Try} result {@link Result} object with the status of the operation
   */
  public Try<Result> grantGranteeGuestAccess(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final String displayName,
      final short rights,
      final long expiry,
      final String secretArgs,
      final String secretPassword) {
    return mailboxManager
        .tryGetMailboxByAccountId(accountId, true)
        .mapTry(
            userMailbox -> {
              final ItemId itemId = itemIdFactory.create(folderId, accountId);
              if (displayName == null || displayName.indexOf('@') < 0) {
                throw ServiceException.INVALID_REQUEST("invalid guest id or password", null);
              }
              // first make sure they didn't accidentally specify "guest" instead of "usr"
              boolean guestGrantee = true;
              String calculatedZimbraId = displayName;
              byte calculatedGrantType = ACL.GRANTEE_GUEST;
              NamedEntry namedEntry = null;
              try {
                namedEntry =
                    granteeService.lookupGranteeByName(
                        calculatedZimbraId, ACL.GRANTEE_USER, operationContext);
                if (namedEntry instanceof MailTarget) {
                  Domain domain = provisioning.getDomain(userMailbox.getAccount());
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
              String calculatedSecret = null;
              if (guestGrantee) {
                calculatedSecret = secretArgs;
                // password is no longer required for external sharing
                if (calculatedSecret == null) {
                  calculatedSecret = secretPassword;
                }
              }
              final Grant grantResult =
                  grantAccess(
                      userMailbox,
                      operationContext,
                      itemId,
                      calculatedZimbraId,
                      calculatedGrantType,
                      rights,
                      calculatedSecret,
                      expiry);

              return new Result(namedEntry, grantResult, calculatedZimbraId);
            });
  }

  /**
   * This method is used to grant access on a folder when ACL grantee type is {@link
   * ACL.GRANTEE_PUBLIC}.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param rights {@link ACL#stringToRights(String)}}
   * @param expiry expiration time
   * @return a {@link Try} result {@link Result} object with the status of the operation
   */
  public Try<Result> grantGranteePublicAccess(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final short rights,
      final long expiry) {
    return mailboxManager
        .tryGetMailboxByAccountId(accountId, true)
        .mapTry(
            userMailbox -> {
              final ItemId itemId = itemIdFactory.create(folderId, accountId);
              String zimbraId = GuestAccount.GUID_PUBLIC;
              long validatedGrantExpiry =
                  new ACLHelper()
                      .validateGrantExpiry(
                          String.valueOf(expiry),
                          accountUtil.getMaxPublicShareLifetime(
                              userMailbox.getAccount(),
                              userMailbox
                                  .getFolderById(operationContext, itemId.getId())
                                  .getDefaultView()));
              final Grant grantResult =
                  grantAccess(
                      userMailbox,
                      operationContext,
                      itemId,
                      zimbraId,
                      ACL.GRANTEE_PUBLIC,
                      rights,
                      null,
                      validatedGrantExpiry);

              return new Result(null, grantResult, zimbraId);
            });
  }

  /**
   * This method is used to grant access on a folder when ACL grantee type is {@link
   * ACL.GRANTEE_AUTHUSER}.
   *
   * @param operationContext an {@link OperationContext}
   * @param accountId the target account zimbra id attribute
   * @param folderId the id of the folder (belonging to the accountId)
   * @param rights {@link ACL#stringToRights(String)}}
   * @param expiry expiration time
   * @return a {@link Try} result {@link Result} object with the status of the operation
   */
  public Try<Result> grantGranteeAuthUserAccess(
      final OperationContext operationContext,
      final String accountId,
      final String folderId,
      final short rights,
      final long expiry) {
    return mailboxManager
        .tryGetMailboxByAccountId(accountId, true)
        .mapTry(
            userMailbox -> {
              final ItemId itemId = itemIdFactory.create(folderId, accountId);
              final String zimbraId = GuestAccount.GUID_AUTHUSER;
              final Grant grantResult =
                  grantAccess(
                      userMailbox,
                      operationContext,
                      itemId,
                      zimbraId,
                      ACL.GRANTEE_AUTHUSER,
                      rights,
                      null,
                      expiry);

              return new Result(null, grantResult, zimbraId);
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

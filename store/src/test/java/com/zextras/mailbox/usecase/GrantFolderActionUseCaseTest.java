package com.zextras.mailbox.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.ldap.GranteeProvider;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.account.MailTarget;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.mail.ItemActionUtil;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.util.AccountUtil;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
class GrantFolderActionUseCaseTest {

  private GrantFolderActionUseCase grantFolderActionUseCase;
  private MailboxManager mailboxManager;
  private ItemActionUtil itemActionUtil;
  private AccountUtil accountUtil;
  private ItemIdFactory itemIdFactory;
  private GranteeProvider granteeProvider;
  private ItemId itemId;
  private String zimbraId;
  private long expiry;
  private String accountId;
  private OperationContext operationContext;
  private String folderId;
  private Mailbox userMailbox;
  private Folder folder;
  private String display;
  private String secretArgs;
  private String secretPassword;
  private String secretAccessKey;
  private short rights;

  @BeforeEach
  void setUp() {

    mailboxManager = mock(MailboxManager.class);
    itemActionUtil = mock(ItemActionUtil.class);
    accountUtil = mock(AccountUtil.class);
    itemIdFactory = mock(ItemIdFactory.class);
    granteeProvider = mock(GranteeProvider.class);

    zimbraId = "id123";
    expiry = 42L;
    accountId = "accountId123";
    secretAccessKey = "accessKey";
    display = "display";
    operationContext = mock(OperationContext.class);
    folderId = accountId + ":1";
    rights = 0;
    userMailbox = mock(Mailbox.class);
    itemId = mock(ItemId.class);
    folder = mock(Folder.class);
    when(folder.getDefaultView()).thenReturn(MailItem.Type.FOLDER);

    grantFolderActionUseCase =
        new GrantFolderActionUseCase(
            mailboxManager, itemActionUtil, accountUtil, itemIdFactory, granteeProvider);
  }

  @Test
  void shouldGrantAccessToAFolder() throws Exception {
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);

    final Try<GrantFolderActionUseCase.Result> grantTry =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_USER,
            zimbraId,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    assertTrue(grantTry.isSuccess());
  }

  @Test
  void shouldSetZimbraIdToGUID_AUTHUSERwhenGrantee_AUTHUSER() throws Exception {
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);

    final Try<GrantFolderActionUseCase.Result> grantTry =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_AUTHUSER,
            zimbraId,
            expiry,
            null,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);
    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(grantTry::get);

    assertEquals(GuestAccount.GUID_AUTHUSER, grantResult.getZimbraId());
  }

  @Test
  void shouldSetZimbraIdToGUI_PUBLICwhenGrantee_PUBLIC() throws Exception {
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);

    final Try<GrantFolderActionUseCase.Result> grantTry =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_PUBLIC,
            zimbraId,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);
    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(grantTry::get);

    assertEquals(GuestAccount.GUID_PUBLIC, grantResult.getZimbraId());
  }

  @Test
  void shouldSetGrantExpirationWhenZimbraIdIsGUID_PUBLIC() throws Exception {
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);
    when(accountUtil.getMaxPublicShareLifetime(any(), eq(MailItem.Type.FOLDER))).thenReturn(420L);

    final Try<GrantFolderActionUseCase.Result> grantTry =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_PUBLIC,
            zimbraId,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    assertDoesNotThrow(grantTry::get);
    verify(itemActionUtil, times(1)).validateGrantExpiry(String.valueOf(expiry), 420L);
  }

  @Test
  void shouldThrowServiceExceptionWhenGrantee_GUESTAndDisplayNull() throws Exception {
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_GUEST,
            zimbraId,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    assertTrue(
        operationResult.isFailure(),
        "Folder should not be granted because display parameter is null");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(ServiceException.class, gotError);
    assertEquals("invalid request: invalid guest id or password", gotError.getMessage());
  }

  @Test
  void shouldPerformLookupGranteeByNameWhenGrantee_GUEST() throws Exception {
    display = "guest@test.com";
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_GUEST,
            zimbraId,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(operationResult::get);

    verify(granteeProvider, times(1))
        .lookupGranteeByName(display, ACL.GRANTEE_USER, operationContext);
    assertEquals(display, grantResult.getZimbraId());
  }

  @Test
  void shouldSetSecretWithSecretArgsWhenGrantee_GUEST() throws Exception {
    display = "guest@test.com";
    secretArgs = "secretArgs";
    NamedEntry namedEntry = mock(NamedEntry.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);
    when(granteeProvider.lookupGranteeByName(display, ACL.GRANTEE_USER, operationContext))
        .thenReturn(namedEntry);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_GUEST,
            zimbraId,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    verify(userMailbox, times(1))
        .grantAccess(
            operationContext,
            itemId.getId(),
            display,
            ACL.GRANTEE_GUEST,
            rights,
            secretArgs,
            expiry);
    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(operationResult::get);
    assertEquals(namedEntry, grantResult.getNamedEntry());
    assertEquals(display, grantResult.getZimbraId());
  }

  @Test
  void shouldSetSecretWithSecretPasswordWhenGrantee_GUEST() throws Exception {
    display = "guest@test.com";
    secretPassword = "secretArgs";
    NamedEntry namedEntry = mock(NamedEntry.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);
    when(granteeProvider.lookupGranteeByName(display, ACL.GRANTEE_USER, operationContext))
        .thenReturn(namedEntry);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_GUEST,
            zimbraId,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    verify(userMailbox, times(1))
        .grantAccess(
            operationContext,
            itemId.getId(),
            display,
            ACL.GRANTEE_GUEST,
            rights,
            secretPassword,
            expiry);
    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(operationResult::get);
    assertEquals(namedEntry, grantResult.getNamedEntry());
    assertEquals(display, grantResult.getZimbraId());
  }

  @Test
  void shouldGrantAccessToGroupWhenGrantee_GUEST() throws Exception {
    Provisioning provisioning = mock(Provisioning.class);
    Provisioning.setInstance(provisioning);

    display = "guest@test.com";
    String domainName = "test.com";
    String namedEntryId = "namedEntryId";

    MailTarget namedEntry = mock(Group.class);
    Account account = mock(Account.class);
    Domain domain = mock(Domain.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);
    when(userMailbox.getAccount()).thenReturn(account);
    when(granteeProvider.lookupGranteeByName(display, ACL.GRANTEE_USER, operationContext))
        .thenReturn(namedEntry);
    when(provisioning.getDomain(account)).thenReturn(domain);
    when(namedEntry.getDomainName()).thenReturn(domainName);
    when(domain.isInternalSharingCrossDomainEnabled()).thenReturn(true);
    when(namedEntry.getId()).thenReturn(namedEntryId);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_GUEST,
            zimbraId,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    verify(userMailbox, times(1))
        .grantAccess(
            operationContext,
            itemId.getId(),
            namedEntryId,
            ACL.GRANTEE_GROUP,
            rights,
            null,
            expiry);
    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(operationResult::get);
    assertEquals(namedEntry, grantResult.getNamedEntry());
    assertEquals(namedEntryId, grantResult.getZimbraId());
  }

  @Test
  void shouldGrantAccessToGuestAccountWhenGrantee_GUEST() throws Exception {
    Provisioning provisioning = mock(Provisioning.class);
    Provisioning.setInstance(provisioning);

    display = "guest@test.com";
    String domainName = "test.com";
    String namedEntryId = "namedEntryId";

    MailTarget namedEntry = mock(GuestAccount.class);
    Account account = mock(Account.class);
    Domain domain = mock(Domain.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);
    when(userMailbox.getAccount()).thenReturn(account);
    when(granteeProvider.lookupGranteeByName(display, ACL.GRANTEE_USER, operationContext))
        .thenReturn(namedEntry);
    when(provisioning.getDomain(account)).thenReturn(domain);
    when(namedEntry.getDomainName()).thenReturn(domainName);
    when(domain.isInternalSharingCrossDomainEnabled()).thenReturn(true);
    when(namedEntry.getId()).thenReturn(namedEntryId);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_GUEST,
            zimbraId,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    verify(userMailbox, times(1))
        .grantAccess(
            operationContext, itemId.getId(), namedEntryId, ACL.GRANTEE_USER, rights, null, expiry);
    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(operationResult::get);
    assertEquals(namedEntry, grantResult.getNamedEntry());
    assertEquals(namedEntryId, grantResult.getZimbraId());
  }

  @Test
  void shouldSetSecretWithSecretAccessKeyWhenGrantee_KEY() throws Exception {
    display = "guest@test.com";

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_KEY,
            zimbraId,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    verify(userMailbox, times(1))
        .grantAccess(
            operationContext,
            itemId.getId(),
            display,
            ACL.GRANTEE_KEY,
            rights,
            secretAccessKey,
            expiry);
    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(operationResult::get);
    assertEquals(display, grantResult.getZimbraId());
  }

  @Test
  void shouldGrantAccessWhenZimbraIdNotNull() throws Exception {
    MailTarget namedEntry = mock(GuestAccount.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);
    when(granteeProvider.lookupGranteeByZimbraId(zimbraId, ACL.GRANTEE_USER))
        .thenReturn(namedEntry);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_USER,
            zimbraId,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    verify(userMailbox, times(1))
        .grantAccess(
            operationContext, itemId.getId(), zimbraId, ACL.GRANTEE_USER, rights, null, expiry);

    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(operationResult::get);
    assertEquals(namedEntry, grantResult.getNamedEntry());
    assertEquals(zimbraId, grantResult.getZimbraId());
  }

  @Test
  void shouldGrantAccessWhenZimbraIdIsNull() throws Exception {
    MailTarget namedEntry = mock(GuestAccount.class);
    String namedEntryId = "namedEntryId";
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);
    when(granteeProvider.lookupGranteeByName(display, ACL.GRANTEE_USER, operationContext))
        .thenReturn(namedEntry);
    when(namedEntry.getId()).thenReturn(namedEntryId);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_USER,
            null,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    verify(userMailbox, times(1))
        .grantAccess(
            operationContext, itemId.getId(), namedEntryId, ACL.GRANTEE_USER, rights, null, expiry);

    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(operationResult::get);
    assertEquals(namedEntry, grantResult.getNamedEntry());
    assertEquals(namedEntryId, grantResult.getZimbraId());
  }

  @Test
  void shouldGrantAccessWhenZimbraIdIsNullAndEntryIsGroup() throws Exception {
    MailTarget namedEntry = mock(Group.class);
    String namedEntryId = "namedEntryId";
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);
    when(granteeProvider.lookupGranteeByName(display, ACL.GRANTEE_USER, operationContext))
        .thenReturn(namedEntry);
    when(namedEntry.getId()).thenReturn(namedEntryId);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_USER,
            null,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    verify(userMailbox, times(1))
        .grantAccess(
            operationContext,
            itemId.getId(),
            namedEntryId,
            ACL.GRANTEE_GROUP,
            rights,
            null,
            expiry);

    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(operationResult::get);
    assertEquals(namedEntry, grantResult.getNamedEntry());
    assertEquals(namedEntryId, grantResult.getZimbraId());
  }

  @Test
  void shouldGrantAccessWhenZimbraIdIsNotFound() throws Exception {
    MailTarget namedEntry = mock(Group.class);
    String namedEntryId = "namedEntryId";
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);
    when(granteeProvider.lookupGranteeByName(display, ACL.GRANTEE_USER, operationContext))
        .thenThrow(AccountServiceException.NO_SUCH_ACCOUNT("account.NO_SUCH_ACCOUNT"));
    when(namedEntry.getId()).thenReturn(namedEntryId);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_USER,
            null,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);

    verify(userMailbox, times(1))
        .grantAccess(
            operationContext, itemId.getId(), display, ACL.GRANTEE_GUEST, rights, null, expiry);

    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(operationResult::get);
    assertEquals(display, grantResult.getZimbraId());
  }

  @Test
  void shouldReturnFailureWhenServiceException() throws Exception {
    MailTarget namedEntry = mock(Group.class);
    String namedEntryId = "namedEntryId";
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);
    when(granteeProvider.lookupGranteeByName(display, ACL.GRANTEE_USER, operationContext))
        .thenThrow(ServiceException.INVALID_REQUEST("This is dummy exception!", null));
    when(namedEntry.getId()).thenReturn(namedEntryId);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            operationContext,
            accountId,
            folderId,
            ACL.GRANTEE_USER,
            null,
            expiry,
            display,
            rights,
            secretArgs,
            secretPassword,
            secretAccessKey);
    assertThrows(ServiceException.class, operationResult::get);
    assertTrue(operationResult.isFailure());
  }
}

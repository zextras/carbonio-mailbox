package com.zextras.mailbox.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.ldap.GranteeProvider;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.mail.ItemActionUtil;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.util.AccountUtil;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
class GrantFolderActionUseCaseTest {

  private static GrantFolderActionUseCase grantFolderActionUseCase;
  private static MailboxManager mailboxManager;
  private static ItemActionUtil itemActionUtil;
  private static AccountUtil accountUtil;
  private static ItemIdFactory itemIdFactory;
  private static GranteeProvider granteeProvider;
  private static ItemId itemId;
  private static String zimbraId;
  private static long expiry;
  private static String randomExpiry;
  private static String accountId;
  private static OperationContext operationContext;
  private static String folderId;
  private static Mailbox userMailbox;
  private static Folder folder;
  private static String display;
  private static String secretArgs;
  private static String secretPassword;

  @BeforeAll
  static void beforeAll() {

    mailboxManager = mock(MailboxManager.class);
    itemActionUtil = mock(ItemActionUtil.class);
    accountUtil = mock(AccountUtil.class);
    itemIdFactory = mock(ItemIdFactory.class);
    granteeProvider = mock(GranteeProvider.class);

    zimbraId = "id123";
    expiry = 42L;
    randomExpiry = "randomExpiry";
    accountId = "accountId123";
    operationContext = mock(OperationContext.class);
    folderId = accountId + ":1";
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
            (byte) 1,
            zimbraId,
            expiry,
            randomExpiry,
            accountId,
            operationContext,
            folderId,
            display,
            secretArgs,
            secretPassword);

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
            ACL.GRANTEE_AUTHUSER,
            zimbraId,
            expiry,
            randomExpiry,
            accountId,
            operationContext,
            folderId,
            display,
            secretArgs,
            secretPassword);
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
            ACL.GRANTEE_PUBLIC,
            zimbraId,
            expiry,
            randomExpiry,
            accountId,
            operationContext,
            folderId,
            display,
            secretArgs,
            secretPassword);
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
            ACL.GRANTEE_PUBLIC,
            zimbraId,
            expiry,
            randomExpiry,
            accountId,
            operationContext,
            folderId,
            display,
            secretArgs,
            secretPassword);

    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(grantTry::get);

    verify(itemActionUtil, times(1)).validateGrantExpiry(randomExpiry, 420L);
  }

  @Test
  void shouldThrowServiceExceptionWhenGrantee_GUESTAndDisplayNull() throws Exception {
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, 1)).thenReturn(folder);

    final Try<GrantFolderActionUseCase.Result> operationResult =
        grantFolderActionUseCase.grant(
            ACL.GRANTEE_GUEST,
            zimbraId,
            expiry,
            randomExpiry,
            accountId,
            operationContext,
            folderId,
            display,
            secretArgs,
            secretPassword);

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
            ACL.GRANTEE_GUEST,
            zimbraId,
            expiry,
            randomExpiry,
            accountId,
            operationContext,
            folderId,
            display,
            secretArgs,
            secretPassword);

    final GrantFolderActionUseCase.Result grantResult = assertDoesNotThrow(operationResult::get);

    verify(granteeProvider, times(1))
        .lookupGranteeByName(display, ACL.GRANTEE_USER, operationContext);

    assertEquals(display, grantResult.getZimbraId());
  }
}

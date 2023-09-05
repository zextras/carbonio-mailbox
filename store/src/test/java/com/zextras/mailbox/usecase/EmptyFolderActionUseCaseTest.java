// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
class EmptyFolderActionUseCaseTest {
  private MailboxManager mailboxManager;
  private EmptyFolderActionUseCase emptyFolderActionUseCase;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);

    emptyFolderActionUseCase = new EmptyFolderActionUseCase(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldBeSuccessAfterEmptyFolder() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    final Try<Void> operationResult =
        emptyFolderActionUseCase.empty(operationContext, accountId, folderId);

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "Folder should be successfully emptied");
  }

  @Test
  void shouldReturnFailureIfTheMailboxDoesntExist() {
    final String accountId = "nonExistingAccount";
    final String folderId = "folderName";

    final Try<Void> operationResult = emptyFolderActionUseCase.empty(null, accountId, folderId);

    assertTrue(
        operationResult.isFailure(),
        "Folder should not be emptied because account id doesn't exist");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(IllegalArgumentException.class, gotError);
    assertEquals("unable to locate the mailbox for the given accountId", gotError.getMessage());
  }

  @Test
  void shouldReturnFailureIfAccountIdIsNull() throws Exception {
    final String accountId = null;
    final String folderId = "folderName";

    when(mailboxManager.getMailboxByAccountId(null, true))
        .thenThrow(new IllegalArgumentException());
    final Try<Void> operationResult = emptyFolderActionUseCase.empty(null, accountId, folderId);

    assertTrue(
        operationResult.isFailure(), "Folder should not be emptied because account id is null");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(IllegalArgumentException.class, gotError);
    assertNull(gotError.getMessage());
  }

  @Test
  void shouldReturnFailureIfTheFolderIdDoesntExist() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    doThrow(MailServiceException.NO_SUCH_FOLDER(folderId))
        .when(userMailbox)
        .emptyFolder(operationContext, 1, false);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    final Try<Void> operationResult =
        emptyFolderActionUseCase.empty(operationContext, accountId, folderId);

    assertTrue(
        operationResult.isFailure(),
        "Folder should not be emptied because folder id doesn't exist");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(MailServiceException.NoSuchItemException.class, gotError);
    assertEquals("no such folder path: account-id123:1", gotError.getMessage());
  }

  @Test
  void shouldReturnFailureIfAccountIdDoesntExist() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    doThrow(MailServiceException.NO_SUCH_FOLDER(folderId))
        .when(userMailbox)
        .emptyFolder(operationContext, 1, false);
    when(mailboxManager.getMailboxByAccountId(accountId, true))
        .thenThrow(
            new FakeAccountServiceException(
                "no such account: " + accountId,
                AccountServiceException.NO_SUCH_ACCOUNT,
                ServiceException.SENDERS_FAULT,
                null));

    final Try<Void> operationResult =
        emptyFolderActionUseCase.empty(operationContext, accountId, folderId);

    assertTrue(
        operationResult.isFailure(),
        "Folder should not be emptied because account id doesn't exist");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(FakeAccountServiceException.class, gotError);
    assertEquals("no such account: account-id123", gotError.getMessage());
  }

  @Test
  void shouldFolderBeEmpty() throws ServiceException {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    emptyFolderActionUseCase.empty(operationContext, accountId, folderId);

    verify(userMailbox, times(1)).emptyFolder(operationContext, itemId.getId(), false);
  }

  @Test
  void shouldEmptyRecursivelySubFolders() throws ServiceException {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    emptyFolderActionUseCase.emptyRecursively(operationContext, accountId, folderId);

    verify(userMailbox, times(1)).emptyFolder(operationContext, itemId.getId(), true);
  }

  @Test
  void shouldPurgeImapIfFolderIsTrashed() throws ServiceException {
    final String accountId = "account-id123";
    final String folderId = accountId + ":3";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(3);

    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    emptyFolderActionUseCase.empty(operationContext, accountId, folderId);

    verify(userMailbox, times(1)).emptyFolder(operationContext, itemId.getId(), false);
    verify(userMailbox, times(1)).purgeImapDeleted(operationContext);
  }

  /**
   * This fake Exception is needed since the constructor of real one ({@link
   * AccountServiceException}) is protected. This is only for testing purposing.
   */
  private static class FakeAccountServiceException extends AccountServiceException {
    protected FakeAccountServiceException(
        String message, String code, boolean isReceiversFault, Throwable cause) {
      super(message, code, isReceiversFault, cause);
    }
  }
}

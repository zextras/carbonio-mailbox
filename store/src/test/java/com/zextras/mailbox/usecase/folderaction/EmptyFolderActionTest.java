// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.usecase.folderaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
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
class EmptyFolderActionTest {
  private MailboxManager mailboxManager;
  private EmptyFolderAction emptyFolderAction;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);

    emptyFolderAction = new EmptyFolderAction(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldBeSuccessAfterEmptyFolder() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.success(userMailbox));
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    final Try<Void> operationResult =
        emptyFolderAction.empty(operationContext, accountId, folderId);

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "Folder should be successfully emptied");
  }

  @Test
  void shouldReturnFailureIfTheMailboxDoesntExist() {
    final String accountId = "nonExistingAccount";
    final String folderId = "folderName";

    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.failure(NoSuchItemException.NO_SUCH_MBOX(accountId)));
    final Try<Void> operationResult = emptyFolderAction.empty(null, accountId, folderId);
    assertTrue(
        operationResult.isFailure(),
        "Folder should not be emptied because account id doesn't exist");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(NoSuchItemException.class, gotError);
  }

  @Test
  void shouldReturnFailureIfAccountIdIsNull() {
    final String accountId = null;
    final String folderId = "folderName";
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.failure(NoSuchItemException.NO_SUCH_MBOX(accountId)));
    final Try<Void> operationResult = emptyFolderAction.empty(null, accountId, folderId);
    assertTrue(
        operationResult.isFailure(), "Folder should not be emptied because account id is null");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(NoSuchItemException.class, gotError);
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
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.success(userMailbox));
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    final Try<Void> operationResult =
        emptyFolderAction.empty(operationContext, accountId, folderId);

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
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.failure(MailServiceException.NO_SUCH_MBOX(accountId)));
    final Try<Void> operationResult =
        emptyFolderAction.empty(operationContext, accountId, folderId);

    assertTrue(
        operationResult.isFailure(),
        "Folder should not be emptied because account id doesn't exist");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(NoSuchItemException.class, gotError);
  }

  @Test
  void shouldFolderBeEmpty() throws ServiceException {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.success(userMailbox));
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    emptyFolderAction.empty(operationContext, accountId, folderId);

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
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.success(userMailbox));
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    emptyFolderAction.emptyRecursively(operationContext, accountId, folderId);

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

    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.success(userMailbox));
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    emptyFolderAction.empty(operationContext, accountId, folderId);

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

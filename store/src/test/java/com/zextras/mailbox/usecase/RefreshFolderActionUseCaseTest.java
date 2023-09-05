package com.zextras.mailbox.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
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
class RefreshFolderActionUseCaseTest {

  private MailboxManager mailboxManager;
  private RefreshFolderActionUseCase refreshFolderActionUseCase;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);
    refreshFolderActionUseCase = new RefreshFolderActionUseCase(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldReturnFailureIfTheMailboxDoesntExist() {
    final String accountId = "nonExistingAccount";
    final String folderId = "folderName";

    final Try<Void> operationResult = refreshFolderActionUseCase.refresh(null, accountId, folderId);

    assertTrue(
        operationResult.isFailure(),
        "Folder should not be refreshed because account id doesn't exist");
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
    final Try<Void> operationResult = refreshFolderActionUseCase.refresh(null, accountId, folderId);

    assertTrue(
        operationResult.isFailure(), "Folder should not be refreshed because account id is null");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(IllegalArgumentException.class, gotError);
    assertNull(gotError.getMessage());
  }

  @Test
  void shouldBeSuccessAfterRefreshFolder() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    refreshFolderActionUseCase.refresh(operationContext, accountId, folderId);

    verify(userMailbox, times(1)).synchronizeFolder(operationContext, itemId.getId());
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
        .synchronizeFolder(operationContext, 1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    final Try<Void> operationResult =
        refreshFolderActionUseCase.refresh(operationContext, accountId, folderId);

    assertTrue(operationResult.isFailure());
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(MailServiceException.NoSuchItemException.class, gotError);
    assertEquals("no such folder path: account-id123:1", gotError.getMessage());
  }
}

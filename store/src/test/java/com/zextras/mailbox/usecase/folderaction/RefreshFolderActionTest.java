package com.zextras.mailbox.usecase.folderaction;

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
class RefreshFolderActionTest {

  private MailboxManager mailboxManager;
  private RefreshFolderAction refreshFolderAction;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);
    refreshFolderAction = new RefreshFolderAction(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldReturnFailureIfTheMailboxDoesntExist() {
    final String accountId = "nonExistingAccount";
    final String folderId = "folderName";
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.failure(MailServiceException.NoSuchItemException.NO_SUCH_MBOX(accountId)));
    final Try<Void> operationResult = refreshFolderAction.refresh(null, accountId, folderId);

    assertTrue(
        operationResult.isFailure(),
        "Folder should not be refreshed because account id doesn't exist");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(MailServiceException.NoSuchItemException.class, gotError);
    assertEquals("no mailbox for account: " + accountId, gotError.getMessage());
  }

  @Test
  void shouldReturnFailureIfAccountIdIsNull() {
    final String accountId = null;
    final String folderId = "folderName";

    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.failure(MailServiceException.NoSuchItemException.NO_SUCH_MBOX(accountId)));
    final Try<Void> operationResult = refreshFolderAction.refresh(null, accountId, folderId);

    assertTrue(
        operationResult.isFailure(), "Folder should not be refreshed because account id is null");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(MailServiceException.NoSuchItemException.class, gotError);
    assertEquals("no mailbox for account: " + accountId, gotError.getMessage());
  }

  @Test
  void shouldBeSuccessAfterRefreshFolder() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.success(userMailbox));
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    refreshFolderAction.refresh(operationContext, accountId, folderId);

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
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.success(userMailbox));
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    final Try<Void> operationResult =
        refreshFolderAction.refresh(operationContext, accountId, folderId);

    assertTrue(operationResult.isFailure());
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(MailServiceException.NoSuchItemException.class, gotError);
    assertEquals("no such folder path: account-id123:1", gotError.getMessage());
  }
}

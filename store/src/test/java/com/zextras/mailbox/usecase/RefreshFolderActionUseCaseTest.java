package com.zextras.mailbox.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

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
        .emptyFolder(operationContext, 1, false);
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

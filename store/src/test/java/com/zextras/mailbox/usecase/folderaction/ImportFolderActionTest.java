package com.zextras.mailbox.usecase.folderaction;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Folder;
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
class ImportFolderActionTest {
  private MailboxManager mailboxManager;
  private ImportFolderAction importFolderAction;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);
    importFolderAction = new ImportFolderAction(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldBeSuccessAfterImportFeedsOnFolder() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final String url = "";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.success(userMailbox));
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    importFolderAction.importFeed(operationContext, accountId, folderId, url);

    verify(userMailbox, times(1)).importFeed(operationContext, itemId.getId(), url, false);
  }

  @Test
  void shouldThrowExceptionWhenImportFeedsOnFolder() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final String url = "notValidUrl";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);
    final Folder folder = mock(Folder.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.success(userMailbox));
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(userMailbox.getFolderById(operationContext, itemId.getId())).thenReturn(folder);
    doThrow(ServiceException.RESOURCE_UNREACHABLE("IOException: ", new RuntimeException()))
        .when(userMailbox)
        .importFeed(operationContext, 1, url, false);

    final Try<Void> operationResult =
        importFolderAction.importFeed(operationContext, accountId, folderId, url);

    assertTrue(operationResult.isFailure(), "Folder should not be imported");
    final Throwable gotError = operationResult.getCause();
    assertInstanceOf(ServiceException.class, gotError);
    assertNotNull(gotError.getMessage());
  }
}

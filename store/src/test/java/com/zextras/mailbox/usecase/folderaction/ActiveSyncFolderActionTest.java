package com.zextras.mailbox.usecase.folderaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActiveSyncFolderActionTest {
  private MailboxManager mailboxManager;
  private ItemIdFactory itemIdFactory;
  private ActiveSyncFolderAction activeSyncFolderAction;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);

    activeSyncFolderAction = new ActiveSyncFolderAction(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldBeSuccessAfterEnableActiveSync() throws ServiceException {
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
        activeSyncFolderAction.enableActiveSync(operationContext, accountId, folderId);

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "ActiveSync should be successfully enabled.");

    verify(userMailbox, times(1)).setActiveSyncDisabled(operationContext, itemId.getId(), false);
  }

  @Test
  void shouldBeSuccessAfterDisableActiveSync() throws ServiceException {
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
        activeSyncFolderAction.disableActiveSync(operationContext, accountId, folderId);

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "ActiveSync should be successfully enabled.");

    verify(userMailbox, times(1)).setActiveSyncDisabled(operationContext, itemId.getId(), true);
  }
}

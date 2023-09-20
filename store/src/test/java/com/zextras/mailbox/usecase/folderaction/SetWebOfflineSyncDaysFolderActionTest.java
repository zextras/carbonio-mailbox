package com.zextras.mailbox.usecase.folderaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SetWebOfflineSyncDaysFolderActionTest {
  private MailboxManager mailboxManager;
  private SetWebOfflineSyncDaysFolderAction setWebOfflineSyncDaysFolderAction;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);

    setWebOfflineSyncDaysFolderAction =
        new SetWebOfflineSyncDaysFolderAction(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldSuccessUpdateWebOfflineSyncDays() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    final Try<Void> operationResult =
        setWebOfflineSyncDaysFolderAction.setWebOfflineSyncDays(
            operationContext, accountId, folderId, 1);

    verify(userMailbox, times(1)).setFolderWebOfflineSyncDays(operationContext, itemId.getId(), 1);
    assertTrue(operationResult.isSuccess());
  }
}

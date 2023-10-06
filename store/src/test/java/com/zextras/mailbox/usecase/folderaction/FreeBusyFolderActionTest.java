package com.zextras.mailbox.usecase.folderaction;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.fb.FreeBusyChangeNotifier;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FreeBusyFolderActionTest {
  private MailboxManager mailboxManager;
  private FreeBusyFolderAction freeBusyFolderAction;
  private ItemIdFactory itemIdFactory;
  private FreeBusyChangeNotifier freeBusyChangeNotifier;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);
    freeBusyChangeNotifier = mock(FreeBusyChangeNotifier.class);
    freeBusyFolderAction =
        new FreeBusyFolderAction(mailboxManager, itemIdFactory, freeBusyChangeNotifier);
  }

  @Test
  void shouldBeSuccessExcludeFreeBusyOnFolder() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.success(userMailbox));
    Try<Void> result =
        freeBusyFolderAction.excludeFreeBusyIntegration(operationContext, accountId, folderId);
    assertTrue(result.isSuccess());
    verify(userMailbox, times(1))
        .alterTag(
            operationContext,
            itemId.getId(),
            MailItem.Type.FOLDER,
            Flag.FlagInfo.EXCLUDE_FREEBUSY,
            false,
            null);
    verify(freeBusyChangeNotifier, times(1)).mailboxChanged(accountId);
  }

  @Test
  void shouldBeSuccessIncludeFreeBusyOnFolder() throws Exception {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(mailboxManager.tryGetMailboxByAccountId(accountId, true))
        .thenReturn(Try.success(userMailbox));

    Try<Void> result =
        freeBusyFolderAction.includeFreeBusyIntegration(operationContext, accountId, folderId);
    assertTrue(result.isSuccess());
    verify(userMailbox, times(1))
        .alterTag(
            operationContext,
            itemId.getId(),
            MailItem.Type.FOLDER,
            Flag.FlagInfo.EXCLUDE_FREEBUSY,
            true,
            null);
    verify(freeBusyChangeNotifier, times(1)).mailboxChanged(accountId);
  }
}

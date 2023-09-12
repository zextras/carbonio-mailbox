package com.zextras.mailbox.usecase;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.cs.fb.FreeBusyProvider;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class FreeBusyFolderActionUseCaseTest {
  private MailboxManager mailboxManager;
  private FreeBusyFolderActionUseCase freeBusyFolderActionUseCase;
  private MockedStatic<FreeBusyProvider> freeBusyProvider;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);

    freeBusyFolderActionUseCase = new FreeBusyFolderActionUseCase(mailboxManager, itemIdFactory);
    freeBusyProvider = mockStatic(FreeBusyProvider.class);
  }

  @AfterEach
  void tearDown() {
    freeBusyProvider.close();
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
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    freeBusyProvider
        .when(() -> FreeBusyProvider.mailboxChanged(accountId))
        .then(freeBusyProvider -> null);
    Try<Void> result =
        freeBusyFolderActionUseCase.excludeFreeBusyIntegration(
            operationContext, accountId, folderId);
    assertTrue(result.isSuccess());
    verify(userMailbox, times(1))
        .alterTag(
            operationContext,
            itemId.getId(),
            MailItem.Type.FOLDER,
            Flag.FlagInfo.EXCLUDE_FREEBUSY,
            false,
            null);
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
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    freeBusyProvider
        .when(() -> FreeBusyProvider.mailboxChanged(accountId))
        .then(freeBusyProvider -> null);
    Try<Void> result =
        freeBusyFolderActionUseCase.includeFreeBusyIntegration(
            operationContext, accountId, folderId);
    assertTrue(result.isSuccess());
    verify(userMailbox, times(1))
        .alterTag(
            operationContext,
            itemId.getId(),
            MailItem.Type.FOLDER,
            Flag.FlagInfo.EXCLUDE_FREEBUSY,
            true,
            null);
  }
}

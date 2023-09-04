package com.zextras.mailbox.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevokeFolderActionUseCaseTest {
  private MailboxManager mailboxManager;
  private RevokeFolderActionUseCase revokeFolderActionUseCase;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);

    revokeFolderActionUseCase = new RevokeFolderActionUseCase(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldBeSuccessAfterRevoke() throws ServiceException {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final String zid = "revokeFolderId";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    final Try<Void> operationResult =
        revokeFolderActionUseCase.revoke(operationContext, accountId, folderId, zid);

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "Folder should be successfully revoked.");

    verify(userMailbox, times(1)).revokeAccess(operationContext, itemId.getId(), zid);
  }

  @Test
  void shouldBeSuccessAfterDeleteMountPoint() throws ServiceException {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final String zid = "revokeFolderId";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    final Try<Void> operationResult =
        revokeFolderActionUseCase.revoke(operationContext, accountId, folderId, zid);

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "Folder should be successfully revoked.");

    verify(userMailbox, times(1)).revokeAccess(operationContext, itemId.getId(), zid);
    // verify(userMailbox, times(1)).deleteMountPoint(operationContext, itemId.getId(), zid);
  }
}

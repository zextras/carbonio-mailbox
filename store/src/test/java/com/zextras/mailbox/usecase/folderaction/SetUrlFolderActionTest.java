package com.zextras.mailbox.usecase.folderaction;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SetUrlFolderActionTest {

  private MailboxManager mailboxManager;
  private SetUrlFolderAction setUrlFolderAction;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);
    setUrlFolderAction = new SetUrlFolderAction(mailboxManager, itemIdFactory);
  }

  @Test
  void shouldBeSuccessAfterSetNonEmptyUrlOnFolder() throws ServiceException {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final String url = "url";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    Try<Void> result =
        setUrlFolderAction.setFolderUrl(operationContext, accountId, folderId, url, true);
    assertTrue(result.isSuccess());
    verify(userMailbox, times(1)).setFolderUrl(operationContext, itemId.getId(), url);
    verify(userMailbox, times(1))
        .alterTag(
            operationContext,
            itemId.getId(),
            MailItem.Type.FOLDER,
            Flag.FlagInfo.EXCLUDE_FREEBUSY,
            true,
            null);
  }

  @Test
  void shouldBeSuccessSynchronizeAfterSetEmptyUrlOnFolder() throws ServiceException {
    final String accountId = "account-id123";
    final String folderId = accountId + ":1";
    final String url = "";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);

    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    Try<Void> result =
        setUrlFolderAction.setFolderUrl(operationContext, accountId, folderId, url, false);
    assertTrue(result.isSuccess());

    verify(userMailbox, times(1)).setFolderUrl(operationContext, itemId.getId(), url);
    verify(userMailbox, times(1)).synchronizeFolder(operationContext, itemId.getId());
  }
}

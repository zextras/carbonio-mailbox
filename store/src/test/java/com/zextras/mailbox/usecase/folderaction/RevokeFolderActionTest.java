package com.zextras.mailbox.usecase.folderaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.service.MountpointService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevokeFolderActionTest {
  private RevokeFolderAction revokeFolderAction;
  private MailboxManager mailboxManager;
  private MountpointService mountpointService;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() throws Exception {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);
    mountpointService = mock(MountpointService.class);

    revokeFolderAction = new RevokeFolderAction(mailboxManager, mountpointService, itemIdFactory);

    MailboxTestUtil.initServer();
  }

  @Test
  void shouldBeSuccessAfterRevoke() throws ServiceException {
    final String accountId = "account-id-123";
    final String folderId = accountId + ":1";
    final String granteeId = "granteeAccount-id-123";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);
    when(itemId.getId()).thenReturn(1);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);

    final Try<Void> operationResult =
        revokeFolderAction.revokeAndDelete(operationContext, accountId, folderId, granteeId);

    verify(userMailbox, times(1)).revokeAccess(operationContext, itemId.getId(), granteeId);
    verify(mountpointService, times(1)).deleteMountpoints(accountId, folderId, granteeId);

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "Folder should be successfully revoked.");
  }
}

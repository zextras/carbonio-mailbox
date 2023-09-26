package com.zextras.mailbox.usecase.folderaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.factory.OperationContextFactory;
import com.zextras.mailbox.usecase.service.MountpointService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevokeFolderActionTest {
  private RevokeFolderAction revokeFolderAction;
  private MailboxManager mailboxManager;
  private MountpointService mountpointService;
  private Provisioning provisioning;
  private OperationContextFactory operationContextFactory;
  private ItemIdFactory itemIdFactory;

  @BeforeEach
  void setUp() throws Exception {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);
    mountpointService = mock(MountpointService.class);
    provisioning = mock(Provisioning.class);
    operationContextFactory = mock(OperationContextFactory.class);

    revokeFolderAction =
        new RevokeFolderAction(
            mailboxManager,
            provisioning,
            mountpointService,
            itemIdFactory,
            operationContextFactory);
  }

  @Test
  void shouldBeSuccessAfterRevoke() throws ServiceException {
    final String accountId = "account-id-123";
    final int folderId = 1;
    final String accountIdFolderId = accountId + ":" + folderId;
    final String granteeId = "granteeAccount-id-123";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId folderItemId = mock(ItemId.class);
    when(folderItemId.getId()).thenReturn(folderId);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(accountIdFolderId, accountId)).thenReturn(folderItemId);

    final OperationContext granteeOpCtx = mock(OperationContext.class);
    final Account granteeAccount = mock(Account.class);
    when(provisioning.getAccountById(granteeId)).thenReturn(granteeAccount);
    when(operationContextFactory.create(granteeAccount)).thenReturn(granteeOpCtx);
    final ItemId granteeRootItemId = mock(ItemId.class);
    when(itemIdFactory.create(String.valueOf(Mailbox.ID_FOLDER_USER_ROOT), granteeId))
        .thenReturn(granteeRootItemId);

    Mountpoint mountpoint = mock(Mountpoint.class);
    final int mountpointId = 100;
    when(mountpoint.getOwnerId()).thenReturn(accountId);
    when(mountpoint.getId()).thenReturn(mountpointId);
    when(mountpoint.getRemoteId()).thenReturn(folderId);
    when(mountpointService.getMountpointsByPath(granteeId, granteeOpCtx, granteeRootItemId))
        .thenReturn(List.of(mountpoint));

    final Try<Void> operationResult =
        revokeFolderAction.revokeAndDelete(
            operationContext, accountId, accountIdFolderId, granteeId);

    Assertions.assertFalse(operationResult.isFailure());

    verify(userMailbox, times(1)).revokeAccess(operationContext, folderItemId.getId(), granteeId);
    verify(mountpointService, times(1))
        .deleteMountpointsByIds(granteeId, granteeOpCtx, List.of(mountpointId));

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "Folder should be successfully revoked.");
  }
}

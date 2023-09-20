package com.zextras.mailbox.usecase.folderaction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.factory.OperationContextFactory;
import com.zextras.mailbox.usecase.service.MountpointService;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevokeFolderActionTest {
  private RevokeFolderAction revokeFolderAction;
  private MailboxManager mailboxManager;
  private MountpointService mountpointService;
  private ItemIdFactory itemIdFactory;
  private OperationContextFactory operationContextFactory;

  @BeforeEach
  void setUp() throws Exception {
    mailboxManager = mock(MailboxManager.class);
    itemIdFactory = mock(ItemIdFactory.class);
    mountpointService = mock(MountpointService.class);
    operationContextFactory = mock(OperationContextFactory.class);

    revokeFolderAction =
        new RevokeFolderAction(
            mailboxManager, mountpointService, itemIdFactory, operationContextFactory);

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
        revokeFolderAction.revoke(operationContext, accountId, folderId, granteeId);

    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "Folder should be successfully revoked.");

    verify(userMailbox, times(1)).revokeAccess(operationContext, itemId.getId(), granteeId);
  }

  @Test
  void shouldBeSuccessAfterDeleteMountPoint() throws ServiceException {
    final String accountId = "account-id-123";
    final int folderItemId = 1;
    final String folderId = String.valueOf(folderItemId);
    final String granteeId = "granteeAccount-id-123";
    final Mailbox userMailbox = mock(Mailbox.class);
    final OperationContext operationContext = mock(OperationContext.class);
    final ItemId itemId = mock(ItemId.class);
    final ItemId granteeRootFolder = mock(ItemId.class);

    Provisioning prov = Provisioning.getInstance();
    Map<String, Object> granteeAttrs = Maps.newHashMap();
    granteeAttrs.put(Provisioning.A_zimbraId, granteeId);
    prov.createAccount("grantee@test.com", "secret", granteeAttrs);
    Account granteeAccount = prov.getAccount("grantee@test.com");
    Mailbox granteeMailbox = MailboxManager.getInstance().getMailboxByAccount(granteeAccount);
    Mountpoint mountpoint =
        granteeMailbox.createMountpoint(
            null,
            Mailbox.ID_FOLDER_USER_ROOT,
            "mountPointFolderName",
            accountId,
            folderItemId,
            null,
            Type.CONVERSATION,
            0,
            (byte) 2,
            false);
    final List<Mountpoint> granteeMounts = List.of(mountpoint);
    final List<Integer> mountPointsIds = List.of(mountpoint.getId());

    when(itemId.getId()).thenReturn(folderItemId);
    when(mailboxManager.getMailboxByAccountId(accountId, true)).thenReturn(userMailbox);
    when(itemIdFactory.create(folderId, accountId)).thenReturn(itemId);
    when(itemIdFactory.create(String.valueOf(Mailbox.ID_FOLDER_USER_ROOT), granteeId))
        .thenReturn(granteeRootFolder);
    when(operationContextFactory.create(granteeAccount)).thenReturn(operationContext);
    when(mountpointService.getMountpointsByPath(
            granteeMailbox, operationContext, granteeRootFolder))
        .thenReturn(granteeMounts);
    when(mountpointService.filterMountpointsByOwnerIdAndRemoteFolderId(
            granteeMounts, accountId, folderId))
        .thenReturn(mountPointsIds);

    final Try<Void> operationResult =
        revokeFolderAction.revoke(operationContext, accountId, folderId, granteeId);
    assertDoesNotThrow(operationResult::get);
    assertTrue(operationResult.isSuccess(), "Folder should be successfully revoked.");

    verify(mountpointService)
        .filterMountpointsByOwnerIdAndRemoteFolderId(granteeMounts, accountId, folderId);
    verify(mountpointService).deleteMountpoints(granteeMailbox, operationContext, mountPointsIds);
  }
}

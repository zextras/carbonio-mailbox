package com.zextras.mailbox.usecase.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.mail.ItemActionHelper;
import com.zimbra.cs.service.util.ItemId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class MountpointServiceTest {
  private Provisioning provisioning;
  private static final String SERVER_NAME = "localhost";
  private MountpointService mountpointService;
  private MailboxManager mailboxManager;
  private MockedStatic<ItemActionHelper> itemActionHelper;

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.setUp();
    itemActionHelper = mockStatic(ItemActionHelper.class);
    mailboxManager = MailboxManager.getInstance();
    provisioning = Provisioning.getInstance(Provisioning.CacheMode.OFF);
    mountpointService = new MountpointService(mailboxManager);
  }

  @AfterEach
  void tearDown() throws ServiceException {
    MailboxTestUtil.tearDown();
    itemActionHelper.close();
  }

  private Mountpoint createDefaultMountPoint(Account owner, Account grantee) throws Exception {
    Mailbox granteeMailbox = mailboxManager.getMailboxByAccount(grantee);
    Mailbox ownerMailbox = mailboxManager.getMailboxByAccount(owner);
    int folderId = 2;
    Folder folder = ownerMailbox.getFolderById(null, folderId);
    String mountPointFolderName = "shared folder";
    return granteeMailbox.createMountpoint(
        null,
        Mailbox.ID_FOLDER_USER_ROOT,
        mountPointFolderName,
        owner.getId(),
        folderId,
        folder.getUuid(),
        Type.CONVERSATION,
        0,
        (byte) 2,
        false);
  }

  @Test
  void shouldGetMountpointsByPath() throws Exception {
    Account grantee =
        provisioning.createAccount(
            "grantee@test.com",
            "secret",
            new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, SERVER_NAME)));
    Account owner =
        provisioning.createAccount(
            "owner@test.com",
            "secret",
            new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, SERVER_NAME)));
    final Mountpoint mountpoint = this.createDefaultMountPoint(owner, grantee);
    ItemId granteeRootFolder =
        new ItemId(String.valueOf(Mailbox.ID_FOLDER_USER_ROOT), grantee.getId());
    List<Mountpoint> granteeMountPoints =
        mountpointService.getMountpointsByPath(
            grantee.getId(), new OperationContext(grantee), granteeRootFolder);

    assertFalse(granteeMountPoints.isEmpty());
    assertEquals(1, granteeMountPoints.size());
    assertEquals(mountpoint, granteeMountPoints.get(0));
  }

  @Test
  void shouldDeleteMountPoints() throws Exception {
    Account grantee =
        provisioning.createAccount(
            "grantee@test.com",
            "secret",
            new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, SERVER_NAME)));
    Account owner =
        provisioning.createAccount(
            "owner@test.com",
            "secret",
            new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, SERVER_NAME)));
    final Mountpoint mountPoint = createDefaultMountPoint(owner, grantee);
    Mailbox granteeMailbox = mailboxManager.getMailboxByAccount(grantee);
    List<Integer> mountpointsIds = List.of(mountPoint.getId());
    final OperationContext granteeOpContext = new OperationContext(grantee);
    mountpointService.deleteMountpointsByIds(grantee.getId(), granteeOpContext, mountpointsIds);
    itemActionHelper.verify(
        () ->
            ItemActionHelper.HARD_DELETE(
                granteeOpContext,
                granteeMailbox,
                SoapProtocol.Soap12,
                mountpointsIds,
                Type.MOUNTPOINT,
                null),
        times(1));
  }
}

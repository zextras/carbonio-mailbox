package com.zextras.mailbox.usecase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.mail.ItemActionHelper;
import com.zimbra.cs.service.util.ItemId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class MountpointServiceTest {
  private MountpointService mountpointService;
  private MockedStatic<ItemActionHelper> itemActionHelper;

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @BeforeEach
  public void setUp() {
    mountpointService = new MountpointService();
    itemActionHelper = mockStatic(ItemActionHelper.class);
  }

  @AfterEach
  void tearDown() throws Exception {
    MailboxTestUtil.clearData();
    itemActionHelper.close();
  }

  @Test
  void shouldGetMountpointsByPath() throws ServiceException {
    Provisioning prov = Provisioning.getInstance();

    Map<String, Object> granteeAttrs = Maps.newHashMap();
    granteeAttrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("grantee@test.com", "secret", granteeAttrs);

    Map<String, Object> ownerAttrs = Maps.newHashMap();
    ownerAttrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
    prov.createAccount("owner@test.com", "secret", ownerAttrs);

    Account granteeAccount = Provisioning.getInstance().get(Key.AccountBy.name, "grantee@test.com");
    Account ownerAccount = Provisioning.getInstance().get(Key.AccountBy.name, "owner@test.com");

    Mailbox granteeMailbox = MailboxManager.getInstance().getMailboxByAccount(granteeAccount);
    Mailbox ownerMailbox = MailboxManager.getInstance().getMailboxByAccount(ownerAccount);

    OperationContext granteeContext = new OperationContext(granteeAccount);
    ItemId granteeRootFolder =
        new ItemId(String.valueOf(Mailbox.ID_FOLDER_USER_ROOT), granteeAccount.getId());

    int folderId = 2;
    Folder folder = ownerMailbox.getFolderById(null, folderId);
    String mountPointFolderName = "shared folder";
    ownerMailbox.grantAccess(
        null,
        folderId,
        granteeAccount.getId(),
        ACL.GRANTEE_USER,
        (short) (ACL.RIGHT_READ | ACL.RIGHT_WRITE),
        null);

    Mountpoint mountpoint =
        granteeMailbox.createMountpoint(
            null,
            Mailbox.ID_FOLDER_USER_ROOT,
            mountPointFolderName,
            ownerAccount.getId(),
            folderId,
            folder.getUuid(),
            Type.CONVERSATION,
            0,
            (byte) 2,
            false);

    List<Mountpoint> mountPoints =
        mountpointService.getMountpointsByPath(granteeMailbox, granteeContext, granteeRootFolder);

    assertFalse(mountPoints.isEmpty());
    assertEquals(1, mountPoints.size());
    assertEquals(mountpoint, mountPoints.get(0));
  }

  @Test
  void shouldDeleteMountPoints() throws ServiceException {
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@test.com", "secret", new HashMap<>());
    Account account = prov.getAccountById(MockProvisioning.DEFAULT_ACCOUNT_ID);
    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
    OperationContext operationContext = new OperationContext(account);
    List<Integer> mountpointsIds = List.of(1, 2, 3);

    mountpointService.deleteMountpoints(mailbox, operationContext, mountpointsIds);

    itemActionHelper.verify(
        () ->
            ItemActionHelper.HARD_DELETE(
                operationContext,
                mailbox,
                SoapProtocol.Soap12,
                mountpointsIds,
                Type.MOUNTPOINT,
                null),
        times(1));
  }

  @Test
  void shouldFilterMountpointsByOwnerIdAndRemoteFolderId() throws ServiceException {
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@test.com", "secret", new HashMap<>());
    String ownerId = MockProvisioning.DEFAULT_ACCOUNT_ID;
    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccountId(ownerId);

    Mountpoint mountpoint =
        mailbox.createMountpoint(
            null,
            Mailbox.ID_FOLDER_USER_ROOT,
            "mountPointFolderName1",
            ownerId,
            1,
            "",
            Type.CONVERSATION,
            0,
            (byte) 2,
            false);

    Mountpoint mountpoint2 =
        mailbox.createMountpoint(
            null,
            Mailbox.ID_FOLDER_USER_ROOT,
            "mountPointFolderName2",
            "222-222",
            1,
            "",
            Type.CONVERSATION,
            0,
            (byte) 2,
            false);

    Mountpoint mountpoint3 =
        mailbox.createMountpoint(
            null,
            Mailbox.ID_FOLDER_USER_ROOT,
            "mountPointFolderName3",
            ownerId,
            2,
            "",
            Type.CONVERSATION,
            0,
            (byte) 2,
            false);
    List<Mountpoint> mountpoints = List.of(mountpoint, mountpoint2, mountpoint3);
    List<Integer> mountPointsIds =
        mountpointService.filterMountpointsByOwnerIdAndRemoteFolderId(mountpoints, ownerId, "1");
    assertFalse(mountPointsIds.isEmpty());
    assertEquals(1, mountPointsIds.size());
    assertEquals(mountpoint.getId(), mountPointsIds.get(0));
  }
}

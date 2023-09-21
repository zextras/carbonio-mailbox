package com.zextras.mailbox.usecase.service;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.factory.OperationContextFactory;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.service.mail.ItemActionHelper;
import com.zimbra.cs.store.StoreManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class MountpointServiceTest {
  private ItemIdFactory itemIdFactory;
  private Account testAccount;
  private OperationContextFactory operationContextFactory;
  private Provisioning provisioning;
  private static final String SERVER_NAME = "localhost";
  private static final int LDAP_PORT = 9091;
  private MountpointService mountpointService;
  private MockedStatic<ItemActionHelper> itemActionHelper;

  //
  //  @Container
  //  GenericContainer ldapContainer =
  //      new GenericContainer<>(DockerImageName.parse("carbonio/ce-ldap-u20:latest"))
  //          .withCreateContainerCmdModifier(
  //              cmd -> {
  //                cmd.withHostName("ldap.mail.local");
  //                cmd.withHostConfig(
  //                    new HostConfig()
  //                        .withPortBindings(
  //                            new PortBinding(Binding.bindPort(389), new ExposedPort(389))));
  //              })
  //          .withExposedPorts(389);

  @BeforeEach
  public void setUp() throws Exception {

    System.setProperty(
        "zimbra.config",
        Objects.requireNonNull(this.getClass().getResource("/localconfig-api-test.xml")).getFile());

    // get top level domain name setup in reset-all.
    // In reset-all, the default domain name is the same as the local server name

    // add list of known DNs

    // add list of known DNs
    InMemoryDirectoryServerConfig ldapServerConfig = null;
    ldapServerConfig =
        new InMemoryDirectoryServerConfig(
            "dc=com",
            "cn=config",
            "cn=defaultExternal,cn=cos,cn=zimbra",
            "cn=default,cn=cos,cn=zimbra",
            "cn=config,cn=zimbra",
            "cn=zimbra");
    ldapServerConfig.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", LDAP_PORT));
    ldapServerConfig.addAdditionalBindCredentials("cn=config", LC.ldap_root_password.value());

    ldapServerConfig.setSchema(null);
    ldapServerConfig.setGenerateOperationalAttributes(true);

    // Create and start the server instance and populate it with an initial set
    // of data from an LDIF file.
    InMemoryDirectoryServer server = new InMemoryDirectoryServer(ldapServerConfig);
    server.importFromLDIF(false, "./build/ldap/config/cn=config.ldif");
    server.importFromLDIF(false, "./build/ldap/zimbra_globalconfig.ldif");
    server.importFromLDIF(false, "./build/ldap/zimbra_defaultcos.ldif");
    server.importFromLDIF(false, "./build/ldap/zimbra_defaultexternalcos.ldif");
    server.importFromLDIF(false, "./build/ldap/carbonio.ldif");
    server.startListening();

    LC.ldap_port.setDefault(LDAP_PORT);
    LC.zimbra_class_database.setDefault(HSQLDB.class.getName());

    DbPool.startup();
    HSQLDB.createDatabase("");

    itemIdFactory = mock(ItemIdFactory.class);
    operationContextFactory = mock(OperationContextFactory.class);
    provisioning = Provisioning.getInstance();
    itemActionHelper = mockStatic(ItemActionHelper.class);
    provisioning.createServer(
        SERVER_NAME,
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
    RedoLogProvider.getInstance().startup();
    StoreManager.getInstance().startup();

    mountpointService = new MountpointService(itemIdFactory, operationContextFactory, provisioning);

    provisioning.createDomain("test.com", new HashMap<>());
    testAccount =
        provisioning.createAccount(
            "test@test.com",
            "password",
            new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraMailHost, SERVER_NAME)));
  }

  @AfterEach
  void tearDown() throws Exception {
    DbPool.shutdown();
    itemActionHelper.close();
  }

  //
  //  @Test
  //  void shouldGetMountpointsByPath() throws ServiceException {
  //    Provisioning prov = Provisioning.getInstance();
  //
  //    Map<String, Object> granteeAttrs = Maps.newHashMap();
  //    granteeAttrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
  //    prov.createAccount("grantee@test.com", "secret", granteeAttrs);
  //
  //    Map<String, Object> ownerAttrs = Maps.newHashMap();
  //    ownerAttrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
  //    prov.createAccount("owner@test.com", "secret", ownerAttrs);
  //
  //    Account granteeAccount = Provisioning.getInstance().get(Key.AccountBy.name,
  // "grantee@test.com");
  //    Account ownerAccount = Provisioning.getInstance().get(Key.AccountBy.name, "owner@test.com");
  //
  //    Mailbox granteeMailbox = MailboxManager.getInstance().getMailboxByAccount(granteeAccount);
  //    Mailbox ownerMailbox = MailboxManager.getInstance().getMailboxByAccount(ownerAccount);
  //
  //    OperationContext granteeContext = new OperationContext(granteeAccount);
  //    ItemId granteeRootFolder =
  //        new ItemId(String.valueOf(Mailbox.ID_FOLDER_USER_ROOT), granteeAccount.getId());
  //
  //    int folderId = 2;
  //    Folder folder = ownerMailbox.getFolderById(null, folderId);
  //    String mountPointFolderName = "shared folder";
  //    ownerMailbox.grantAccess(
  //        null,
  //        folderId,
  //        granteeAccount.getId(),
  //        ACL.GRANTEE_USER,
  //        (short) (ACL.RIGHT_READ | ACL.RIGHT_WRITE),
  //        null);
  //
  //    Mountpoint mountpoint =
  //        granteeMailbox.createMountpoint(
  //            null,
  //            Mailbox.ID_FOLDER_USER_ROOT,
  //            mountPointFolderName,
  //            ownerAccount.getId(),
  //            folderId,
  //            folder.getUuid(),
  //            Type.CONVERSATION,
  //            0,
  //            (byte) 2,
  //            false);
  //
  //    List<Mountpoint> mountPoints =
  //        mountpointService.getMountpointsByPath(granteeMailbox, granteeContext,
  // granteeRootFolder);
  //
  //    assertFalse(mountPoints.isEmpty());
  //    assertEquals(1, mountPoints.size());
  //    assertEquals(mountpoint, mountPoints.get(0));
  //  }
  //
  //  @Test
  //  void shouldDeleteMountPoints() throws ServiceException {
  //    Provisioning prov = Provisioning.getInstance();
  //    prov.createAccount("test@test.com", "secret", new HashMap<>());
  //    Account account = prov.getAccountById(MockProvisioning.DEFAULT_ACCOUNT_ID);
  //    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(account);
  //    OperationContext operationContext = new OperationContext(account);
  //    List<Integer> mountpointsIds = List.of(1, 2, 3);
  //
  //    mountpointService.deleteMountpointsByIds(mailbox, operationContext, mountpointsIds);
  //
  //    itemActionHelper.verify(
  //        () ->
  //            ItemActionHelper.HARD_DELETE(
  //                operationContext,
  //                mailbox,
  //                SoapProtocol.Soap12,
  //                mountpointsIds,
  //                Type.MOUNTPOINT,
  //                null),
  //        times(1));
  //  }

  @Test
  void shouldFilterMountpointsByOwnerIdAndRemoteFolderId() throws ServiceException {
    final String ownerId = testAccount.getId();
    Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccountId(ownerId);

    Mountpoint mountpoint1 =
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
    List<Mountpoint> mountpoints = List.of(mountpoint1);

    List<Integer> mountPointsIds =
        mountpointService.filterMountpointsByOwnerIdAndRemoteFolderId(mountpoints, ownerId, "1");
    assertFalse(mountPointsIds.isEmpty());
    assertEquals(1, mountPointsIds.size());
    assertEquals(mountpoint1.getId(), mountPointsIds.get(0));
  }
}

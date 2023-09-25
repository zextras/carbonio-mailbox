package com.zextras.mailbox.usecase.service;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.CacheMode;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Mountpoint;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.service.mail.ItemActionHelper;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.store.StoreManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class MountpointServiceTest {
  private Provisioning provisioning;
  private static final String SERVER_NAME = "localhost";
  private static final int LDAP_PORT = 9091;
  private MountpointService mountpointService;
  private MailboxManager mailboxManager;
  private MockedStatic<ItemActionHelper> itemActionHelper;
  private InMemoryDirectoryServer inMemoryLdapServer;

  @BeforeEach
  public void setUp() throws Exception {
    System.setProperty(
        "zimbra.config",
        Objects.requireNonNull(this.getClass().getResource("/localconfig-api-test.xml")).getFile());

    InMemoryDirectoryServerConfig ldapServerConfig =
        new InMemoryDirectoryServerConfig(
            "dc=com",
            "cn=config",
            "cn=defaultExternal,cn=cos,cn=zimbra",
            "cn=default,cn=cos,cn=zimbra",
            "cn=config,cn=zimbra",
            "cn=zimbra");
    ldapServerConfig.addAdditionalBindCredentials("cn=config", LC.ldap_root_password.value());
    ldapServerConfig.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", LDAP_PORT));
    ldapServerConfig.setSchema(null);
    ldapServerConfig.setGenerateOperationalAttributes(true);

    inMemoryLdapServer = new InMemoryDirectoryServer(ldapServerConfig);
    inMemoryLdapServer.importFromLDIF(true, "./build/ldap/config/cn=config.ldif");
    inMemoryLdapServer.importFromLDIF(false, "./build/ldap/zimbra_globalconfig.ldif");
    inMemoryLdapServer.importFromLDIF(false, "./build/ldap/zimbra_defaultcos.ldif");
    inMemoryLdapServer.importFromLDIF(false, "./build/ldap/zimbra_defaultexternalcos.ldif");
    inMemoryLdapServer.importFromLDIF(false, "./build/ldap/carbonio.ldif");
    inMemoryLdapServer.startListening();
    // update password for admin
    inMemoryLdapServer
        .getConnection()
        .modify(
            "uid=zimbra,cn=admins,cn=zimbra",
            new Modification(
                ModificationType.REPLACE, "userPassword", LC.zimbra_ldap_password.value()));

    LC.ldap_port.setDefault(LDAP_PORT);
    LC.zimbra_class_database.setDefault(HSQLDB.class.getName());

    DbPool.startup();
    HSQLDB.createDatabase("");

    provisioning = Provisioning.getInstance(CacheMode.OFF);
    itemActionHelper = mockStatic(ItemActionHelper.class);
    mailboxManager = MailboxManager.getInstance();
    provisioning.createServer(
        SERVER_NAME,
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
    RedoLogProvider.getInstance().startup();
    StoreManager.getInstance().startup();

    mountpointService = new MountpointService(mailboxManager);
    provisioning.createDomain("test.com", new HashMap<>());
  }

  @AfterEach
  void tearDown() throws Exception {
    inMemoryLdapServer.clear();
    inMemoryLdapServer.shutDown(true);
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

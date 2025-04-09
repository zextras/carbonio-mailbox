package com.zextras.mailbox.util;

import static com.zimbra.cs.account.GuestAccount.GUID_PUBLIC;
import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.zextras.carbonio.message_broker.MessageBrokerClient;
import com.zextras.mailbox.messageBroker.MessageBrokerFactory;
import com.zextras.mailbox.util.InMemoryLdapServer.Builder;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import com.zimbra.cs.mailbox.*;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.store.StoreManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.mail.internet.MimeMessage;


public class MailboxTestUtil {
  public static final String SERVER_NAME = "localhost";
  public static final String DEFAULT_DOMAIN = "test.com";
  public static final String DEFAULT_DOMAIN_ID = "f4806430-b434-4e93-9357-a02d9dd796b8";

  public static InMemoryLdapServer getInMemoryLdapServer() {
    return inMemoryLdapServer;
  }

  private static InMemoryLdapServer inMemoryLdapServer;

  private static MessageBrokerClient mockedMessageBrokerClient;

  private MailboxTestUtil() {}

  /**
   * Sets up all possible environment variables to make the mailbox work:
   * - loads native library
   * - Uses localconfig-api-test.xml as source for {@link LC}
   * - creates a server with name {@link #SERVER_NAME}
   * - creates a domain with name {@link #DEFAULT_DOMAIN}
   * - Starts LDAP and the database and all possible dependencies. If you find some are missing add them.
   *
   * @throws Exception
   */
  public static void setUp() throws Exception {
    System.setProperty("zimbra.native.required", "false");
    System.setProperty(
        "zimbra.config",
        Objects.requireNonNull(
                com.zimbra.cs.mailbox.MailboxTestUtil.class.getResource(
                    "/localconfig-api-test.xml"))
            .getFile());

    final var mailboxHome = Files.createTempDirectory("mailbox_home_");
    LC.zimbra_home.setDefault(mailboxHome.toAbsolutePath().toString());

    var ldapPort = PortUtil.findFreePort();

    LC.ldap_port.setDefault(ldapPort);

    inMemoryLdapServer = new Builder()
        .withLdapPort(ldapPort)
        .build();
    inMemoryLdapServer.start();
    inMemoryLdapServer.initializeBasicData();

    LC.zimbra_class_database.setDefault(HSQLDB.class.getName());
    initData();

    DbPool.startup();
    HSQLDB.createDatabase();

    RedoLogProvider.getInstance().startup();
    StoreManager.getInstance().startup();
    RightManager.getInstance();
    ScheduledTaskManager.startup();
  }

  /**
   * Stops the {@link #inMemoryLdapServer} and {@link RedoLogProvider} The goal is to cleanup
   * the system before starting another one. If some clean task is missing consider adding it.
   */
  public static void tearDown() throws ServiceException {
    inMemoryLdapServer.clear();
    RedoLogProvider.getInstance().shutdown();
    inMemoryLdapServer.shutDown(true);
  }

  public static void clearData() throws Exception {
    inMemoryLdapServer.clear();
    HSQLDB.clearDatabase();
  }

  public static void initData() throws Exception {
    inMemoryLdapServer.initializeBasicData();
    var provisioning = Provisioning.getInstance(Provisioning.CacheMode.OFF);
    var lmtpPort = PortUtil.findFreePort();
    var pop3Port = PortUtil.findFreePort();
    var imapPort = PortUtil.findFreePort();
    final var server =
        provisioning.createServer(
            SERVER_NAME,
            new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
    server.setLmtpBindPort(lmtpPort);

    server.setPop3BindPort(pop3Port);
    server.setPop3SSLServerEnabled(false);

    server.setImapBindPort(imapPort);
    server.setImapSSLServerEnabled(false);
    var domain = provisioning.createDomain(DEFAULT_DOMAIN, new HashMap<>());
    domain.setId(DEFAULT_DOMAIN_ID);
    mockMessageBrokerClient();
  }

  private static void mockMessageBrokerClient() {
    if(mockedMessageBrokerClient == null) {
      var messageBrokerClient = Mockito.mock(MessageBrokerClient.class);
      var mockedMessageBrokerFactory = Mockito.mockStatic(MessageBrokerFactory.class, Mockito.CALLS_REAL_METHODS);
      mockedMessageBrokerFactory.when(MessageBrokerFactory::getMessageBrokerClientInstance).thenReturn(messageBrokerClient);
      mockedMessageBrokerClient = messageBrokerClient;
    }
  }

  /** Performs actions on an account. Start with {@link #shareWith(Account)} */
  public static class AccountAction {

    private final Account account;
    private final MailboxManager mailboxManager;
    private final RightManager rightManager;

    /**
     * Saves a message in current Account mailbox. It is useful when you want to "simulate"
     * receiving of a message.
     *
     * @param message message to save
     * @return saved {@link javax.mail.Message}
     * @throws ServiceException
     * @throws IOException
     */
    public Message saveMsgInInbox(javax.mail.Message message) throws ServiceException, IOException {
      final var parsedMessage = new ParsedMessage((MimeMessage) message, false);
      final var deliveryOptions =
          new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
      return mailboxManager
          .getMailboxByAccount(account)
          .addMessage(null, parsedMessage, deliveryOptions, null);
    }

    public Message saveDraft(ParsedMessage message) throws ServiceException, IOException {
      return mailboxManager
          .getMailboxByAccount(account)
          .saveDraft(new OperationContext(account), message, Mailbox.ID_AUTO_INCREMENT);
    }

    public static class Factory {
      private final MailboxManager mailboxManager;
      private final RightManager rightManager;

      public Factory(MailboxManager mailboxManager, RightManager rightManager) {
        this.mailboxManager = mailboxManager;
        this.rightManager = rightManager;
      }

      public AccountAction forAccount(Account account) {
        return new AccountAction(account, mailboxManager, rightManager);
      }

      public static Factory getDefault() throws ServiceException {
        return new Factory(MailboxManager.getInstance(), RightManager.getInstance());
      }
    }

    private AccountAction(
        Account account, MailboxManager mailboxManager, RightManager rightManager) {
      this.account = account;
      this.mailboxManager = mailboxManager;
      this.rightManager = rightManager;
    }

    /**
     * Shares current account with target
     *
     * @param target AKA "delegated"
     * @throws ServiceException
     */
    public AccountAction shareWith(Account target) throws ServiceException {
      grantRightTo(target, rightManager.getRight(Right.RT_sendAs));
      grantFolderRightTo(target, "rw", Mailbox.ID_FOLDER_USER_ROOT);
      return this;
    }

    public AccountAction grantFolderRightTo(Account target, String rights, int folderId)
        throws ServiceException {
      mailboxManager
          .getMailboxByAccount(account)
          .grantAccess(
              null, folderId, target.getId(), ACL.GRANTEE_USER, ACL.stringToRights(rights), null);
      return this;
    }

    public AccountAction grantRightTo(Account target, Right right) throws ServiceException {
      final Set<ZimbraACE> aces = new HashSet<>();
      aces.add(
          new ZimbraACE(
              account.getId(),
              GranteeType.GT_USER,
              RightManager.getInstance().getRight(right.getName()),
              RightModifier.RM_CAN_DELEGATE,
              null));
      ACLUtil.grantRight(Provisioning.getInstance(), target, aces);
      return this;
    }


    public AccountAction grantPublicFolderRight( int folderId, String rights) throws ServiceException {
      mailboxManager
              .getMailboxByAccount(account)
              .grantAccess(
                      null, folderId, GUID_PUBLIC, ACL.GRANTEE_PUBLIC, ACL.stringToRights(rights), null);
      return this;
    }
  }

  public static class AccountCreator {

    private final Provisioning provisioning;
    private String username = UUID.randomUUID().toString();
    private String password = "password";
    private String domain = DEFAULT_DOMAIN;
    private final Map<String, Object> attributes =
        new HashMap<>(Map.of(Provisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME));

    private AccountCreator(Provisioning provisioning) {
      this.provisioning = provisioning;
    }

    public static class Factory {
      private final Provisioning provisioning;

      public Factory(Provisioning provisioning) {
        this.provisioning = provisioning;
      }

      public static Factory getDefault() {
        return new AccountCreator.Factory(Provisioning.getInstance());
      }

      public AccountCreator get() {
        return new AccountCreator(provisioning);
      }
    }

    public AccountCreator withUsername(String username) {
      this.username = username;
      return this;
    }

    public AccountCreator withPassword(String password) {
      this.password = password;
      return this;
    }

    public AccountCreator withDomain(String domain) {
      this.domain = domain;
      return this;
    }

    public AccountCreator withAttribute(String name, Object value) {
      this.attributes.put(name, value);
      return this;
    }

    public AccountCreator asGlobalAdmin() {
      this.attributes.put(ZAttrProvisioning.A_zimbraIsAdminAccount, "TRUE");
      return this;
    }

    public Account create() throws ServiceException {
      return provisioning.createAccount(this.username + "@" + this.domain, password, attributes);
    }
  }

}

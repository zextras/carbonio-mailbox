package com.zextras.mailbox.util;

import static com.zimbra.cs.account.Provisioning.SERVICE_MAILCLIENT;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLDB;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.ScheduledTaskManager;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.redolog.RedoLogProvider;
import com.zimbra.cs.store.StoreManager;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.mail.internet.MimeMessage;

/**
 * This utility class allows easy setup for the Mailbox environment using {@link #setUp()} method.
 * To clean up th environment remember to call {@link #tearDown()}. It uses an {@link
 * InMemoryDirectoryServer} and an in memory database using {@link HSQLDB} as dependencies.
 */
public class MailboxTestUtil {
  public static final int LDAP_PORT = 1389;
  public static final String SERVER_NAME = "localhost";
  public static final String DEFAULT_DOMAIN = "test.com";
  private static InMemoryDirectoryServer inMemoryDirectoryServer;

  private MailboxTestUtil() {}

  /**
   * Sets up all possible environment variables to make the mailbox operate. - loads native library
   * - Uses localconfig-api-test.xml as source for {@link LC} - creates a server with name {@link
   * #SERVER_NAME} - creates a domain with name {@link #DEFAULT_DOMAIN} Starts LDAP and the database
   * and all possible dependencies. If you find some are missing add them.
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

    inMemoryDirectoryServer = InMemoryLdapServerTestUtil.createInMemoryDirectoryServer(LDAP_PORT);
    inMemoryDirectoryServer.startListening();

    LC.ldap_port.setDefault(LDAP_PORT);
    LC.zimbra_class_database.setDefault(HSQLDB.class.getName());

    DbPool.startup();
    HSQLDB.createDatabase("");

    final Provisioning provisioning = Provisioning.getInstance(Provisioning.CacheMode.OFF);
    provisioning.createServer(
        SERVER_NAME,
        new HashMap<>(Map.of(ZAttrProvisioning.A_zimbraServiceEnabled, SERVICE_MAILCLIENT)));
    provisioning.createDomain(DEFAULT_DOMAIN, new HashMap<>());

    RedoLogProvider.getInstance().startup();
    StoreManager.getInstance().startup();
    RightManager.getInstance();
    ScheduledTaskManager.startup();
  }

  /**
   * Stops the {@link #inMemoryDirectoryServer} and {@link RedoLogProvider} The goal is to cleanup
   * the system before starting another one. If some clean task is missing consider adding it.
   */
  public static void tearDown() throws ServiceException {
    inMemoryDirectoryServer.clear();
    RedoLogProvider.getInstance().shutdown();
    inMemoryDirectoryServer.shutDown(true);
  }

  /**
   * Creates a basic account with domain {@link #DEFAULT_DOMAIN} and {@link #SERVER_NAME}. You can
   * pass extra attributes if needed, for example to make the user an admin. If you pass a {@link
   * ZAttrProvisioning#A_zimbraMailHost} you will override the default server of the user.
   *
   * @param extraAttrs attributes to add on top of default one
   * @return created account
   * @throws ServiceException
   */
  public static Account createRandomAccountForDefaultDomain(Map<String, Object> extraAttrs) throws ServiceException {
    return createAccountWithDefaultDomain(UUID.randomUUID().toString(), extraAttrs);
  }

  /**
   * Performs actions on an account.
   * Start with {@link #shareWith(Account)}
   *
   */
  public static class AccountAction {

    private final Account account;
    private final MailboxManager mailboxManager;

    public static AccountAction forAccount(Account account) throws ServiceException {
      return new AccountAction(account, MailboxManager.getInstance());
    }

    private AccountAction(Account account, MailboxManager mailboxManager) {
      this.account = account;
      this.mailboxManager = mailboxManager;
    }

    /**
     * Shares current account with target
     * @param target AKA "delegated"
     * @throws ServiceException
     */
    public AccountAction shareWith(Account target) throws ServiceException {
      final Set<ZimbraACE> aces = new HashSet<>();
      aces.add(
          new ZimbraACE(
              account.getId(),
              GranteeType.GT_USER,
              RightManager.getInstance().getRight(Right.RT_sendAs),
              RightModifier.RM_CAN_DELEGATE,
              null));
      final Mailbox sharedMailbox = mailboxManager.getMailboxByAccount(account);
      sharedMailbox.grantAccess(
          null, Mailbox.ID_FOLDER_ROOT, target.getId(), ACL.GRANTEE_USER, ACL.stringToRights("rw"), null);
      ACLUtil.grantRight(Provisioning.getInstance(), target, aces);
      return this;
    }
  }

  public static class AccountCreator {

    private final Provisioning provisioning;
    private String username = UUID.randomUUID().toString();
    private String password = "password";
    private String domain = DEFAULT_DOMAIN;
    private Map<String, Object> defaultAttributes = new HashMap<>(Map.of(Provisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME));
    private Map<String, Object> extraAttributes = Collections.emptyMap();

    public AccountCreator(Provisioning provisioning) {
      this.provisioning = provisioning;
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

    public AccountCreator withExtraAttributes(Map<String, Object> extraAttributes) {
      this.extraAttributes = extraAttributes;
      return this;
    }

    public AccountCreator withDefaultAttributes(Map<String, Object> defaultAttributes) {
      this.defaultAttributes = defaultAttributes;
      return this;
    }

    public Account create() throws ServiceException {
      final Map<String, Object> attributes = new HashMap<String, Object>(defaultAttributes);
      attributes.putAll(extraAttributes);
      defaultAttributes.putAll(extraAttributes);
      return provisioning
          .createAccount(this.username + "@" + this.domain, password, attributes);
    }
  }

  /**
   * @return {@link Account}
   * @deprecated should use {@link AccountCreator}
   * @throws ServiceException
   */
  @Deprecated()
  public static Account createAccountWithDefaultDomain(String username, Map<String, Object> extraAttrs) throws ServiceException {
    final HashMap<String, Object> attrs =
        new HashMap<>(Map.of(Provisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME));
    attrs.putAll(extraAttrs);
    return Provisioning.getInstance()
        .createAccount(username + "@" + MailboxTestUtil.DEFAULT_DOMAIN, "password", attrs);
  }

  /**
   * @return {@link Account}
   * @deprecated should use {@link AccountCreator}
   * @throws ServiceException
   */
  @Deprecated()
  public static Account createRandomAccountForDefaultDomain() throws ServiceException {
    return createRandomAccountForDefaultDomain(Collections.emptyMap());
  }

  /**
   * Saves a message in a mailbox. Used to simulate receiving of a message.
   *
   * @param mailbox mailbox where to save the message
   * @param message message to save
   * @return saved {@link javax.mail.Message}
   * @throws ServiceException
   * @throws IOException
   */
  public static Message saveMsgInInbox(Mailbox mailbox, javax.mail.Message message)
      throws ServiceException, IOException {
    final ParsedMessage parsedMessage = new ParsedMessage((MimeMessage) message, false);
    final DeliveryOptions deliveryOptions =
        new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
    return mailbox.addMessage(null, parsedMessage, deliveryOptions, null);
  }
}

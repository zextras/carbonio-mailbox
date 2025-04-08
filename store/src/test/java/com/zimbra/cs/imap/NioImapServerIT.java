package com.zimbra.cs.imap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.MailboxManager;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.apache.commons.net.imap.AuthenticatingIMAPClient;
import org.apache.commons.net.imap.IMAPClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NioImapServerIT {

  private static IMAPClient imapClient;
  private static Account account;
  private static NioImapServer imapServer;
	private static final String FIRST_MESSAGE_BODY = "This is the first message";
  private static final String SECOND_MESSAGE_BODY = "This is the second message";

  @BeforeAll
  public static void setUp() throws Exception {
    MailboxTestUtil.setUp();

    ImapConfig config = new ImapConfig(false);
    MeterRegistry mockMeterRegistry = mock(MeterRegistry.class);
    imapServer = new NioImapServer(config, mockMeterRegistry);
    imapClient = new AuthenticatingIMAPClient();

    Provisioning provisioning = Provisioning.getInstance();
    provisioning.getConfig().setImapBindPort(7143);
    provisioning.getConfig().setImapCleartextLoginEnabled(true);

    AccountCreator.Factory accountFactory = new AccountCreator.Factory(provisioning);
    account = accountFactory.get().create();

    AccountAction.Factory accountActionFactory = new AccountAction.Factory(MailboxManager.getInstance(), RightManager.getInstance());

		MimeMessage firstMessage = new MimeMessage(Session.getInstance(new Properties()));
    firstMessage.setSubject("Test email 1");
    firstMessage.setText(FIRST_MESSAGE_BODY);
    accountActionFactory.forAccount(account).saveMsgInInbox(firstMessage);

		MimeMessage secondMessage = new MimeMessage(Session.getInstance(new Properties()));
    secondMessage.setSubject("Test email 2");
    secondMessage.setText(SECOND_MESSAGE_BODY);
    accountActionFactory.forAccount(account).saveMsgInInbox(secondMessage);

    imapServer.start();
    imapClient.connect("localhost", 7143);
  }

  @AfterAll
  public static void tearDown() throws Exception {
    imapServer.stop();
    MailboxTestUtil.tearDown();
  }

  @Test
  void shouldLogin() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    var replyString = imapClient.getReplyString();

    assertTrue(replyString.contains("OK"), "Login failed: " + replyString);
  }

  @Test
  void shouldListFolders() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.list("", "*");
    var replyString = imapClient.getReplyString();

    var foldersOutput =
        "* LIST (\\HasNoChildren \\Drafts) \"/\" \"Drafts\"\r\n"
            + "* LIST (\\HasNoChildren) \"/\" \"INBOX\"\r\n"
            + "* LIST (\\NoInferiors \\Junk) \"/\" \"Junk\"\r\n"
            + "* LIST (\\HasNoChildren \\Sent) \"/\" \"Sent\"\r\n"
            + "* LIST (\\HasNoChildren \\Trash) \"/\" \"Trash\"\r\n";

    assertTrue(replyString.contains(foldersOutput), "List folders failed: " + replyString);
    assertTrue(replyString.contains("OK LIST completed"), "List folders failed: " + replyString);
  }

  @Test
  void shouldDisplayTheFirstMessage() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.select("INBOX");
    imapClient.fetch("1","(BODY[TEXT])");
    var replyString = imapClient.getReplyString();

    assertTrue(replyString.contains(FIRST_MESSAGE_BODY));
  }

  @Test
  void shouldDisplayTheSecondMessage() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.select("INBOX");
    imapClient.fetch("2","(BODY[TEXT])");
    var replyString = imapClient.getReplyString();

    assertTrue(replyString.contains(SECOND_MESSAGE_BODY), replyString);
  }

  @Test
  void shouldDisplayAllMessages() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.select("INBOX");
    imapClient.fetch("1:2","(BODY[TEXT])");
    var replyString = imapClient.getReplyString();

    assertTrue(replyString.contains(FIRST_MESSAGE_BODY), replyString);
    assertTrue(replyString.contains(SECOND_MESSAGE_BODY), replyString);
  }
}

package com.zimbra.cs.imap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.util.AccountAction;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.apache.commons.net.imap.AuthenticatingIMAPClient;
import org.apache.commons.net.imap.IMAPClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NioImapServerIT extends MailboxTestSuite {

  private static final String FIRST_MESSAGE_BODY = "This is the first message";
  private static final String SECOND_MESSAGE_BODY = "This is the second message";
  private static final String FIRST_MESSAGE_SUBJECT = "Test email 1";
  private static IMAPClient imapClient;
  private static Account account;
  private static NioImapServer imapServer;
  private static final int IMAP_PORT = 7143;

  @BeforeAll
  static void setUp() throws Exception {

    ImapConfig config = new ImapConfig(false);
    MeterRegistry mockMeterRegistry = mock(MeterRegistry.class);
    imapServer = new NioImapServer(config, mockMeterRegistry);
    imapClient = new AuthenticatingIMAPClient();

    Provisioning provisioning = Provisioning.getInstance();
    provisioning.getConfig().setImapBindPort(IMAP_PORT);
    provisioning.getConfig().setImapCleartextLoginEnabled(true);

    account = createAccount().create();

    AccountAction.Factory accountActionFactory =
        getAccountActionFactory();

    MimeMessage firstMessage = new MimeMessage(Session.getInstance(new Properties()));
    firstMessage.setSubject(FIRST_MESSAGE_SUBJECT);
    firstMessage.setText(FIRST_MESSAGE_BODY);
    accountActionFactory.forAccount(account).saveMsgInInbox(firstMessage);

    MimeMessage secondMessage = new MimeMessage(Session.getInstance(new Properties()));
    secondMessage.setSubject("Test email 2");
    secondMessage.setText(SECOND_MESSAGE_BODY);
    accountActionFactory.forAccount(account).saveMsgInInbox(secondMessage);

    imapServer.start();
    imapClient.connect("localhost", IMAP_PORT);
  }

  @AfterAll
  static void tearDown() throws Exception {
    imapServer.stop();
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
        new String[] {
          "* LIST (\\HasNoChildren \\Drafts) \"/\" \"Drafts\"",
          "* LIST (\\HasNoChildren) \"/\" \"INBOX\"",
          "* LIST (\\NoInferiors \\Junk) \"/\" \"Junk\"",
          "* LIST (\\HasNoChildren \\Sent) \"/\" \"Sent\"",
          "* LIST (\\HasNoChildren \\Trash) \"/\" \"Trash\"",
          "* LIST (\\HasNoChildren \\Archive) \"/\" \"Archive\"",
        };

    Arrays.stream(foldersOutput).forEach(folder -> assertTrue(replyString.contains(folder), "Missing folder: " + folder + " in response: " + replyString));

    assertTrue(replyString.contains("OK LIST completed"));
  }

  @Test
  void shouldDisplayTheFirstMessage() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.select("INBOX");
    imapClient.fetch("1", "(BODY[TEXT])");
    var replyString = imapClient.getReplyString();

    assertTrue(replyString.contains(FIRST_MESSAGE_BODY));
  }

  @Test
  void shouldDisplayTheSecondMessage() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.select("INBOX");
    imapClient.fetch("2", "(BODY[TEXT])");
    var replyString = imapClient.getReplyString();

    assertTrue(replyString.contains(SECOND_MESSAGE_BODY), replyString);
  }

  @Test
  void shouldDisplayAllMessages() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.select("INBOX");
    imapClient.fetch("1:2", "(BODY[TEXT])");
    var replyString = imapClient.getReplyString();

    assertTrue(replyString.contains(FIRST_MESSAGE_BODY), replyString);
    assertTrue(replyString.contains(SECOND_MESSAGE_BODY), replyString);
  }

  @Test
  void shouldCreateFolder() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.create("TestFolder");
    var replyString = imapClient.getReplyString();

    assertTrue(replyString.contains("OK CREATE completed"));

    imapClient.list("", "*");
    var listReplyString = imapClient.getReplyString();

    assertTrue(listReplyString.contains("TestFolder"));
  }

  @Test
  void shouldSearchFirstMessageBySubject() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.select("INBOX");
    imapClient.search("SUBJECT \"" + FIRST_MESSAGE_SUBJECT + "\"");
    var replyStrings = imapClient.getReplyStrings();


    assertEquals( "* SEARCH 1", replyStrings[0]);
    assertTrue(replyStrings[1].contains("OK SEARCH completed"));
  }

  @Test
  void searchBySubjectShouldReturnMatchingMessages() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.select("INBOX");
    imapClient.search("SUBJECT \"Test Email\"");
    var replyStrings = imapClient.getReplyStrings();

    assertEquals( "* SEARCH 1 2", replyStrings[0]);
    assertTrue(replyStrings[1].contains("OK SEARCH completed"));
  }

  @Test
  void shouldDeleteFolder() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.create("TestFolderToBeDeleted");
    imapClient.delete("TestFolderToBeDeleted");
    var replyString = imapClient.getReplyString();

    assertTrue(replyString.contains("OK DELETE completed"));

    imapClient.list("", "*");
    var listReplyString = imapClient.getReplyString();

    assertFalse(listReplyString.contains("TestFolderToBeDeleted"));
  }

  @Test
  void shouldReturnSpecialUseCapability() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.sendCommand("CAPABILITY");

    var replyString = imapClient.getReplyString();

    assertTrue(
        replyString.contains("SPECIAL-USE"),
        "CAPABILITY response does not contain SPECIAL-USE: " + replyString);
  }

  @Test
  void shouldListSpecialUseFolders() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.sendCommand("LIST \"\" \"*\" RETURN (SPECIAL-USE)");

    var replyString = imapClient.getReplyString();

    assertTrue(replyString.contains("* LIST (\\HasNoChildren \\Drafts) \"/\" \"Drafts\""));
    assertTrue(replyString.contains("* LIST (\\HasNoChildren) \"/\" \"INBOX\""));
    assertTrue(replyString.contains("* LIST (\\NoInferiors \\Junk) \"/\" \"Junk\""));
    assertTrue(replyString.contains("* LIST (\\HasNoChildren \\Sent) \"/\" \"Sent\""));
    assertTrue(replyString.contains("* LIST (\\HasNoChildren \\Trash) \"/\" \"Trash\""));
    assertTrue(replyString.contains("* LIST (\\HasNoChildren \\Archive) \"/\" \"Archive\""));
  }

  @Test
  void shouldAppendMessage() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    imapClient.sendCommand("SELECT INBOX");

    String subject = "SearchTest123";
    String message =
        "From: test@example.com\r\n"
            + "To: test@example.com\r\n"
            + "Subject: "
            + subject
            + "\r\n"
            + "Date: Mon, 7 Apr 2025 10:00:00 +0000\r\n"
            + "\r\n"
            + "This is a test message body.\r\n";

    imapClient.append(
        "INBOX", "{\"" + message.length() + "\"}", "Mon, 7 Apr 2025 10:00:00 +0000", message);
    var appendReply = imapClient.getReplyString();
    assertTrue(appendReply.contains("OK"), "APPEND failed: " + appendReply);

    imapClient.search("SUBJECT \"" + subject + "\"");
    var replyStrings = imapClient.getReplyStrings();
    assertEquals("* SEARCH 3", replyStrings[0]);

    imapClient.sendCommand("FETCH 3 (BODY[HEADER.FIELDS (SUBJECT)])");
    var fetchReply = imapClient.getReplyString();

    assertTrue(fetchReply.contains(subject));
  }
}

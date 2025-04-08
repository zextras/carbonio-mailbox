package com.zimbra.cs.imap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import org.apache.commons.net.imap.AuthenticatingIMAPClient;
import org.apache.commons.net.imap.IMAPClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NioImapServerIT {

  private static IMAPClient imapClient;
  private static Account account;
  private static NioImapServer imapServer;

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

    var expectedOutput =
        "* LIST (\\HasNoChildren \\Drafts) \"/\" \"Drafts\"\r\n"
            + "* LIST (\\HasNoChildren) \"/\" \"INBOX\"\r\n"
            + "* LIST (\\NoInferiors \\Junk) \"/\" \"Junk\"\r\n"
            + "* LIST (\\HasNoChildren \\Sent) \"/\" \"Sent\"\r\n"
            + "* LIST (\\HasNoChildren \\Trash) \"/\" \"Trash\"\r\n"
            + "AAAB OK LIST completed\r\n";

    assertEquals(expectedOutput, replyString, "List folders failed: " + replyString);
  }
}

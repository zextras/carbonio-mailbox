package com.zimbra.cs.imap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import org.apache.commons.net.imap.AuthenticatingIMAPClient;
import org.apache.commons.net.imap.IMAPClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NioImapServerIT {

  private NioImapServer imapServer;
  private IMAPClient imapClient;
  private Account account;

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.setUp();
  }

  @AfterAll
  public static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
  }

  @BeforeEach
  public void setUp() throws ServiceException, IOException {
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

  @Test
  void shouldLogin() throws IOException {
    imapClient.login(account.getName(), account.getUserPassword());
    var replyString = imapClient.getReplyString();

    assertTrue(replyString.contains("OK"), "Login failed: " + replyString);
  }
}

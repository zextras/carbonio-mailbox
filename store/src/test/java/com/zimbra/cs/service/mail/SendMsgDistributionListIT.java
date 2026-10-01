// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.util.PortUtil;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.util.JMSession;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SendMsgDistributionListIT extends MailboxTestSuite {

  private static final String DENIED_MESSAGE = "Sender is not allowed to email this distribution list";

  private static Provisioning provisioning;
  private static GreenMail greenMail;

  @BeforeAll
  static void setUpClass() throws Exception {
    final int smtpPort = PortUtil.findFreePort();
    greenMail =
        new GreenMail(
            new ServerSetup[] {
              new ServerSetup(smtpPort, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
            });
    greenMail.start();
    provisioning = Provisioning.getInstance();
    provisioning.getLocalServer().setSmtpPort(smtpPort);
  }

  @AfterAll
  static void tearDownClass() {
    greenMail.stop();
  }

  @BeforeEach
  void setUp() {
    greenMail.reset();
  }

  @Test
  void sendMsg_shouldFail_whenSenderNotAllowedToSendToDistributionList() throws Exception {
    var sender = createAccount().create();
    var someoneElse = createAccount().create();
    var dl = createDistributionList();
    grantSendToDistList(dl, someoneElse);

    var exception =
        assertThrows(MailServiceException.class, () -> sendMsg(sender, dl.getName()));

    assertSame(MailServiceException.SEND_ABORTED_ADDRESS_FAILURE, exception.getCode());
    assertTrue(exception.getMessage().contains(DENIED_MESSAGE));
    assertEquals(dl.getName(), exception.getArgumentValue("invalid"));
    assertEquals(0, greenMail.getReceivedMessages().length);
    assertTrue(sentMessagesOf(sender).isEmpty());
  }

  @Test
  void sendMsg_shouldSucceed_whenSenderGrantedSendToDistList() throws Exception {
    var sender = createAccount().create();
    var dl = createDistributionList();
    grantSendToDistList(dl, sender);

    sendMsg(sender, dl.getName());

    assertEquals(1, greenMail.getReceivedMessages().length);
    assertEquals(1, sentMessagesOf(sender).size());
  }

  @Test
  void sendMsg_shouldSucceed_whenDistributionListHasNoGrants() throws Exception {
    var sender = createAccount().create();
    var dl = createDistributionList();

    sendMsg(sender, dl.getName());

    assertEquals(1, greenMail.getReceivedMessages().length);
  }

  @Test
  void sendMsg_shouldAbortForAllRecipients_whenOneDistributionListIsDenied() throws Exception {
    var sender = createAccount().create();
    var someoneElse = createAccount().create();
    var recipient = createAccount().create();
    var dl = createDistributionList();
    grantSendToDistList(dl, someoneElse);

    var exception =
        assertThrows(
            MailServiceException.class, () -> sendMsg(sender, recipient.getName(), dl.getName()));

    assertSame(MailServiceException.SEND_ABORTED_ADDRESS_FAILURE, exception.getCode());
    assertEquals(recipient.getName(), exception.getArgumentValue("unsent"));
    assertEquals(0, greenMail.getReceivedMessages().length);
  }

  @Test
  void partialSend_shouldDeliverToAllowedRecipientsAndReportDeniedList() throws Exception {
    var sender = createAccount().create();
    var someoneElse = createAccount().create();
    var recipient = createAccount().create();
    var dl = createDistributionList();
    grantSendToDistList(dl, someoneElse);

    var exception =
        assertThrows(
            MailServiceException.class,
            () -> sendPartial(sender, recipient.getName(), dl.getName()));

    assertSame(MailServiceException.SEND_PARTIAL_ADDRESS_FAILURE, exception.getCode());
    assertEquals(dl.getName(), exception.getArgumentValue("invalid"));
    var received = greenMail.getReceivedMessages();
    assertEquals(1, received.length);
    assertEquals(recipient.getName(), greenMail.getUserManager().listUser().iterator().next().getEmail());
  }

  @Test
  void partialSend_shouldSendNothing_whenEveryRecipientIsADeniedList() throws Exception {
    var sender = createAccount().create();
    var someoneElse = createAccount().create();
    var dl = createDistributionList();
    grantSendToDistList(dl, someoneElse);

    var exception =
        assertThrows(MailServiceException.class, () -> sendPartial(sender, dl.getName()));

    assertSame(MailServiceException.SEND_PARTIAL_ADDRESS_FAILURE, exception.getCode());
    assertEquals(0, greenMail.getReceivedMessages().length);
    assertTrue(sentMessagesOf(sender).isEmpty());
  }

  private static void sendMsg(Account sender, String... recipients) throws Exception {
    var recipientElements = new StringBuilder();
    for (String recipient : recipients) {
      recipientElements.append(",{\"t\":\"t\",\"a\":\"%s\"}".formatted(recipient));
    }
    var requestBody =
        """
        {"m":{"su":{"_content":"Hello"},
        "e":[{"t":"f","a":"%s"}%s],
        "mp":[{"ct":"text/plain","content":{"_content":"Test"}}]}}
        """
            .formatted(sender.getName(), recipientElements);
    new SendMsg().handle(Element.parseJSON(requestBody), contextOf(sender));
  }

  private static void sendPartial(Account sender, String... recipients) throws Exception {
    var mailbox = MailboxManager.getInstance().getMailboxByAccount(sender);
    MimeMessage message = new Mime.FixedMimeMessage(JMSession.getSmtpSession(sender));
    message.setFrom(new InternetAddress(sender.getName()));
    for (String recipient : recipients) {
      message.addRecipient(RecipientType.TO, new InternetAddress(recipient));
    }
    message.setSubject("Hello");
    message.setText("Test");
    message.saveChanges();
    mailbox
        .getMailSender()
        .setSendPartial(true)
        .sendMimeMessage(new OperationContext(sender), mailbox, message);
  }

  private static Map<String, Object> contextOf(Account account) throws Exception {
    Map<String, Object> context = new HashMap<>();
    context.put(
        SoapEngine.ZIMBRA_CONTEXT,
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(account),
            account.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12));
    return context;
  }

  private static List<MailItem> sentMessagesOf(Account account) throws Exception {
    return MailboxManager.getInstance()
        .getMailboxByAccount(account)
        .getItemList(null, MailItem.Type.MESSAGE, Mailbox.ID_FOLDER_SENT);
  }

  private static DistributionList createDistributionList() throws Exception {
    return provisioning.createDistributionList(
        UUID.randomUUID() + "@" + DEFAULT_DOMAIN_NAME, new HashMap<>());
  }

  private static void grantSendToDistList(DistributionList dl, Account grantee) throws Exception {
    ACLUtil.grantRight(
        provisioning,
        dl,
        Set.of(
            new ZimbraACE(
                grantee.getId(),
                GranteeType.GT_USER,
                RightManager.getInstance().getRight(Right.RT_sendToDistList),
                null,
                null)));
  }
}

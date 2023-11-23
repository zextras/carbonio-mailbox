// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zextras.mailbox.util.MailboxTestUtil.AccountCreator;
import com.zimbra.common.mime.shim.JavaMailMimeMessage;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.zmime.ZSharedFileInputStream;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.service.mail.GetMsg;
import com.zimbra.cs.service.mail.SendMsg;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.cs.util.JMSession;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.GetMsgRequest;
import com.zimbra.soap.mail.message.GetMsgResponse;
import com.zimbra.soap.mail.message.SendMsgRequest;
import com.zimbra.soap.mail.message.SendMsgResponse;
import com.zimbra.soap.mail.type.EmailAddrInfo;
import com.zimbra.soap.mail.type.MsgSpec;
import com.zimbra.soap.mail.type.MsgToSend;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GetMsgTest {

  public static String zimbraServerDir = "";
  private static Account testAccount;
  private static Account sender;
  private static Account receiver;
  private static Account shared;
  private static GreenMail mta;
  private static AccountAction.Factory accountActionFactory;
  private static AccountCreator.Factory accountCreatorFactory;
  private static MailboxManager mailboxManager;

  @BeforeAll
  public static void setUp() throws Exception {
    MailboxTestUtil.setUp();
    mailboxManager = MailboxManager.getInstance();
    accountActionFactory = new AccountAction.Factory(mailboxManager, RightManager.getInstance());
    accountCreatorFactory = new AccountCreator.Factory(Provisioning.getInstance());
    mta =
        new GreenMail(
            new ServerSetup[] {
              new ServerSetup(
                  SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP),
              new ServerSetup(9000, "127.0.0.1", ServerSetup.PROTOCOL_IMAP)
            });
    final Provisioning provisioning = Provisioning.getInstance();
    sender = accountCreatorFactory.get().withUsername("test").create();
    shared = accountCreatorFactory.get().withUsername("shared").create();
    accountActionFactory.forAccount(shared).shareWith(sender);
    receiver = accountCreatorFactory.get().withUsername("rcpt").create();
    testAccount = accountCreatorFactory.get().create();
    mta.start();
  }

  @AfterAll
  public static void tearDown() throws Exception {
    mta.stop();
    MailboxTestUtil.tearDown();
  }

  @Test
  void testMsgMaxAttr() throws Exception {
    MimeMessage message =
        new JavaMailMimeMessage(
            JMSession.getSession(),
            new ZSharedFileInputStream(zimbraServerDir + "data/TestMailRaw/1"));
    final Message msg = accountActionFactory.forAccount(testAccount).saveMsgInInbox(message);
    Element request = new Element.XMLElement(MailConstants.GET_MSG_REQUEST);
    Element action = request.addElement(MailConstants.E_MSG);
    action.addAttribute(MailConstants.A_ID, msg.getId());
    action.addAttribute(MailConstants.A_MAX_INLINED_LENGTH, 10);

    Element response =
        new GetMsg()
            .handle(request, ServiceTestUtil.getRequestContext(testAccount))
            .getElement(MailConstants.E_MSG);
    assertEquals(
        response
            .getElement(MailConstants.E_MIMEPART)
            .getElement(MailConstants.E_CONTENT)
            .getText()
            .length(),
        10);
  }

  @Test
  void testMsgView() throws Exception {
    MimeMessage message =
        new JavaMailMimeMessage(
            JMSession.getSession(),
            new ZSharedFileInputStream(zimbraServerDir + "data/unittest/email/bug_75163.txt"));
    final Message msg = accountActionFactory.forAccount(testAccount).saveMsgInInbox(message);
    Element request = new Element.XMLElement(MailConstants.GET_MSG_REQUEST);
    Element action = request.addElement(MailConstants.E_MSG);
    action.addAttribute(MailConstants.A_ID, msg.getId());

    Element response =
        new GetMsg()
            .handle(request, ServiceTestUtil.getRequestContext(testAccount))
            .getElement(MailConstants.E_MSG);
    List<Element> mimeParts = response.getElement(MailConstants.E_MIMEPART).listElements();
    // test plain text view
    for (Element elt : mimeParts) {
      if (elt.getAttribute(MailConstants.A_BODY, null) != null) {
        assertEquals(elt.getAttribute(MailConstants.A_CONTENT_TYPE), "text/plain");
        break;
      }
    }

    action.addAttribute(MailConstants.A_WANT_HTML, 1);
    response =
        new GetMsg()
            .handle(request, ServiceTestUtil.getRequestContext(testAccount))
            .getElement(MailConstants.E_MSG);
    mimeParts = response.getElement(MailConstants.E_MIMEPART).listElements();
    // test HTML view
    for (Element elt : mimeParts) {
      if (elt.getAttribute(MailConstants.A_BODY, null) != null) {
        assertEquals(elt.getAttribute(MailConstants.A_CONTENT_TYPE), "text/html");
        break;
      }
    }
  }

  @Test
  void testMsgHeaderN() throws Exception {
    MimeMessage message =
        new JavaMailMimeMessage(
            JMSession.getSession(),
            new ZSharedFileInputStream(zimbraServerDir + "data/unittest/email/bug_75163.txt"));
    final Message msg = accountActionFactory.forAccount(testAccount).saveMsgInInbox(message);
    Element request = new Element.XMLElement(MailConstants.GET_MSG_REQUEST);
    Element action = request.addElement(MailConstants.E_MSG);
    action.addAttribute(MailConstants.A_ID, msg.getId());
    action
        .addElement(MailConstants.A_HEADER)
        .addAttribute(MailConstants.A_ATTRIBUTE_NAME, "Return-Path");

    Element response =
        new GetMsg()
            .handle(request, ServiceTestUtil.getRequestContext(testAccount))
            .getElement(MailConstants.E_MSG);
    List<Element> headerN = response.listElements(MailConstants.A_HEADER);
    for (Element elt : headerN) {
      assertEquals(elt.getText(), "foo@example.com");
    }
  }

  /**
   * Sends a sample email with Disposition-Notification-To sender
   *
   * @param sender sender of email
   * @param recipients recipients of the email
   * @return {@link Element}
   * @throws Exception
   */
  private SendMsgResponse sendSampleEmailWithNotification(Account sender, List<Account> recipients)
      throws Exception {
    final SendMsgRequest sendMsgRequest = new SendMsgRequest();
    final MsgToSend msgToSend = new MsgToSend();
    msgToSend.setSubject("Test");
    final EmailAddrInfo readReceiptAddress = new EmailAddrInfo(sender.getName());
    readReceiptAddress.setAddressType("n");
    final List<EmailAddrInfo> emailAddresses = new ArrayList<>();
    emailAddresses.add(readReceiptAddress);
    recipients.forEach(
        rcpt -> {
          final EmailAddrInfo emailAddrInfo = new EmailAddrInfo(rcpt.getName());
          emailAddrInfo.setAddressType("t");
          emailAddresses.add(emailAddrInfo);
        });
    msgToSend.setEmailAddresses(emailAddresses);
    msgToSend.setContent("Hello there");
    sendMsgRequest.setMsg(msgToSend);
    Element request = JaxbUtil.jaxbToElement(sendMsgRequest);
    return JaxbUtil.elementToJaxb(
        new SendMsg().handle(request, ServiceTestUtil.getRequestContext(sender)),
        SendMsgResponse.class);
  }

  @Test
  void shouldReturnReadReceiptOnDelegatedRequest() throws Exception {
    this.sendSampleEmailWithNotification(sender, List.of(receiver, shared));
    final Mailbox sharedMbox = mailboxManager.getMailboxByAccount(shared);
    final MimeMessage receiverMsg = mta.getReceivedMessagesForDomain(receiver.getName())[0];
    final GetMsgResponse receiverMessage =
        this.getMsgRequest(
            String.valueOf(accountActionFactory.forAccount(receiver).saveMsgInInbox(receiverMsg).getId()),
            ServiceTestUtil.getRequestContext(receiver));
    Assertions.assertEquals(
        1,
        (int)
            receiverMessage.getMsg().getEmails().stream()
                .filter(emailInfo -> Objects.equals("n", emailInfo.getAddressType()))
                .count());
    final MimeMessage sharedMsg = mta.getReceivedMessagesForDomain(shared.getName())[0];
    final GetMsgResponse sharedAccountMessage =
        this.getMsgRequest(
            String.valueOf(accountActionFactory.forAccount(shared).saveMsgInInbox(sharedMsg).getId()),
            ServiceTestUtil.getSOAPDelegatedContext(sender, shared));

    Assertions.assertEquals(
        1,
        (int)
            sharedAccountMessage.getMsg().getEmails().stream()
                .filter(emailInfo -> Objects.equals("n", emailInfo.getAddressType()))
                .count());
    final Folder sharedInboxFolder = sharedMbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
    Assertions.assertEquals(1, sharedInboxFolder.getSize());
  }

  /**
   * Execute a {@link GetMsg} Request
   *
   * @param messageId id of the message to get
   * @param ctxt context
   * @return {@link GetMsgResponse}
   * @throws ServiceException
   */
  private GetMsgResponse getMsgRequest(String messageId, Map<String, Object> ctxt)
      throws ServiceException {
    final GetMsgRequest getMsgRequest = new GetMsgRequest(new MsgSpec(messageId));
    final Element getMsg = new GetMsg().handle(JaxbUtil.jaxbToElement(getMsgRequest), ctxt);
    return JaxbUtil.elementToJaxb(getMsg, GetMsgResponse.class);
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Maps;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.mime.shim.JavaMailMimeMessage;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.zmime.ZSharedFileInputStream;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.mime.ParsedMessage;
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
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private static MailboxManager mailboxManager;

  @BeforeAll
  public static void setUp() throws Exception {
    MailboxTestUtil.setUp();
    mta =
        new GreenMail(
            new ServerSetup[]{
                new ServerSetup(
                    SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP),
                new ServerSetup(9000, "127.0.0.1", ServerSetup.PROTOCOL_IMAP)
            });
    final Provisioning provisioning = Provisioning.getInstance();
    sender =
        provisioning.createAccount(
            "test@" + MailboxTestUtil.DEFAULT_DOMAIN,
            "password",
            Maps.newHashMap(Map.of(Provisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME)));
    shared =
        provisioning.createAccount(
            "shared@" + MailboxTestUtil.DEFAULT_DOMAIN,
            "password",
            Maps.newHashMap(Map.of(Provisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME)));
    final Set<ZimbraACE> aces = new HashSet<>();
    aces.add(
        new ZimbraACE(
            sender.getId(),
            GranteeType.GT_USER,
            RightManager.getInstance().getRight(Right.RT_sendAs),
            RightModifier.RM_CAN_DELEGATE,
            null));
    aces.add(
        new ZimbraACE(
            sender.getId(),
            GranteeType.GT_USER,
            RightManager.getInstance().getRight(Right.RT_loginAs),
            RightModifier.RM_CAN_DELEGATE,
            null));
    mailboxManager = MailboxManager.getInstance();
    final Mailbox sharedMailbox = mailboxManager.getMailboxByAccount(shared);

    sharedMailbox.grantAccess(
        null,
        Mailbox.ID_FOLDER_AUTO_CONTACTS,
        sender.getId(),
        ACL.GRANTEE_USER,
        ACL.stringToRights("rwi"),
        null);

    ACLUtil.grantRight(Provisioning.getInstance(), shared, aces);
    receiver =
        provisioning.createAccount(
            "rcpt@" + MailboxTestUtil.DEFAULT_DOMAIN,
            "password",
            Maps.newHashMap(Map.of(Provisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME)));
    testAccount = MailboxTestUtil.createAccountDefaultDomain(Maps.newHashMap());
    mta.start();
  }

  @AfterAll
  public static void tearDown() throws Exception {
    mta.stop();
    MailboxTestUtil.tearDown();
  }

  @Test
  void testMsgMaxAttr() throws Exception {
    Mailbox mbox = MailboxManager.getInstance()
        .getMailboxByAccount(testAccount);
    MimeMessage message = new JavaMailMimeMessage(JMSession.getSession(),
        new ZSharedFileInputStream(zimbraServerDir + "data/TestMailRaw/1"));
    final Message msg = this.saveMsgInInbox(mbox, message);
    Element request = new Element.XMLElement(MailConstants.GET_MSG_REQUEST);
    Element action = request.addElement(MailConstants.E_MSG);
    action.addAttribute(MailConstants.A_ID, msg.getId());
    action.addAttribute(MailConstants.A_MAX_INLINED_LENGTH, 10);

    Element response = new GetMsg().handle(request,
        ServiceTestUtil.getRequestContext(mbox.getAccount())).getElement(MailConstants.E_MSG);
    assertEquals(
        response.getElement(MailConstants.E_MIMEPART).getElement(MailConstants.E_CONTENT).getText()
            .length(), 10);
  }

  @Test
  void testMsgView() throws Exception {
    Mailbox mbox = mailboxManager
        .getMailboxByAccount(testAccount);
    MimeMessage message = new JavaMailMimeMessage(JMSession.getSession(),
        new ZSharedFileInputStream(zimbraServerDir + "data/unittest/email/bug_75163.txt"));
    final Message msg = this.saveMsgInInbox(mbox, message);
    Element request = new Element.XMLElement(MailConstants.GET_MSG_REQUEST);
    Element action = request.addElement(MailConstants.E_MSG);
    action.addAttribute(MailConstants.A_ID, msg.getId());

    Element response = new GetMsg().handle(request,
        ServiceTestUtil.getRequestContext(mbox.getAccount())).getElement(MailConstants.E_MSG);
    List<Element> mimeParts = response.getElement(MailConstants.E_MIMEPART).listElements();
    // test plain text view
    for (Element elt : mimeParts) {
      if (elt.getAttribute(MailConstants.A_BODY, null) != null) {
        assertEquals(elt.getAttribute(MailConstants.A_CONTENT_TYPE), "text/plain");
        break;
      }
    }

    action.addAttribute(MailConstants.A_WANT_HTML, 1);
    response = new GetMsg().handle(request, ServiceTestUtil.getRequestContext(mbox.getAccount()))
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
    Mailbox mbox = MailboxManager.getInstance()
        .getMailboxByAccount(testAccount);
    MimeMessage message = new JavaMailMimeMessage(JMSession.getSession(),
        new ZSharedFileInputStream(zimbraServerDir + "data/unittest/email/bug_75163.txt"));
    final Message msg = this.saveMsgInInbox(mbox, message);
    Element request = new Element.XMLElement(MailConstants.GET_MSG_REQUEST);
    Element action = request.addElement(MailConstants.E_MSG);
    action.addAttribute(MailConstants.A_ID, msg.getId());
    action.addElement(MailConstants.A_HEADER)
        .addAttribute(MailConstants.A_ATTRIBUTE_NAME, "Return-Path");

    Element response = new GetMsg().handle(request,
        ServiceTestUtil.getRequestContext(mbox.getAccount())).getElement(MailConstants.E_MSG);
    List<Element> headerN = response.listElements(MailConstants.A_HEADER);
    for (Element elt : headerN) {
      assertEquals(elt.getText(), "foo@example.com");
    }
  }

  @Test
  void shouldReturnReadReceiptOnDelegatedRequest() throws Exception {
    final SendMsgRequest sendMsgRequest = new SendMsgRequest();
    final MsgToSend msgToSend = new MsgToSend();
    msgToSend.setSubject("Test");
    final EmailAddrInfo sharedAddress = new EmailAddrInfo(shared.getName());
    sharedAddress.setAddressType("t");
    final EmailAddrInfo receiverAddress = new EmailAddrInfo(receiver.getName());
    receiverAddress.setAddressType("t");
    final EmailAddrInfo readReceiptAddress = new EmailAddrInfo(sender.getName());
    readReceiptAddress.setAddressType("n");
    msgToSend.setEmailAddresses(List.of(sharedAddress, receiverAddress, readReceiptAddress));
    msgToSend.setContent("Hello there");
    sendMsgRequest.setMsg(msgToSend);
    Element request = JaxbUtil.jaxbToElement(sendMsgRequest);
    final Mailbox sharedMbox = mailboxManager.getMailboxByAccount(shared);
    final Mailbox receiverMbox = mailboxManager.getMailboxByAccount(receiver);
    JaxbUtil.elementToJaxb(
        new SendMsg().handle(request, ServiceTestUtil.getRequestContext(sender)),
        SendMsgResponse.class);
    final MimeMessage receiverMsg =
        mta.getReceivedMessagesForDomain(receiverAddress.getAddress())[0];
    Assertions.assertEquals(
        4,
        this.getMsgRequest(
                String.valueOf(this.saveMsgInInbox(receiverMbox, receiverMsg).getId()),
                ServiceTestUtil.getRequestContext(receiver))
            .getMsg()
            .getEmails()
            .size());
    final MimeMessage sharedMsg = mta.getReceivedMessagesForDomain(sharedAddress.getAddress())[0];
    Assertions.assertEquals(
        4,
        this.getMsgRequest(
                String.valueOf(this.saveMsgInInbox(sharedMbox, sharedMsg).getId()),
                ServiceTestUtil.getSOAPDelegatedContext(sender, shared))
            .getMsg()
            .getEmails()
            .size());

    final Folder sharedInboxFolder = sharedMbox.getFolderById(null, Mailbox.ID_FOLDER_INBOX);
    Assertions.assertEquals(1, sharedInboxFolder.getSize());
  }

  /**
   * Saves a message in a mailbox.
   * Used to simulate receiving of a message.
   *
   * @param mailbox mailbox where to save the message
   * @param message message to save
   * @return saved {@link javax.mail.Message}
   * @throws ServiceException
   * @throws IOException
   */
  private Message saveMsgInInbox(Mailbox mailbox, javax.mail.Message message)
      throws ServiceException, IOException {
    final ParsedMessage parsedMessage = new ParsedMessage((MimeMessage) message, false);
    final DeliveryOptions deliveryOptions = new DeliveryOptions().setFolderId(
        Mailbox.ID_FOLDER_INBOX);
    return mailbox.addMessage(null, parsedMessage, deliveryOptions, null);
  }

  /**
   * Execute a {@link GetMsg} Request
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

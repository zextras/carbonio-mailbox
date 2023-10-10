// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Maps;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.zmime.ZContentType;
import com.zimbra.common.zmime.ZMimeMessage;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTest;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.util.JMSession;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.GetContactsRequest;
import com.zimbra.soap.mail.message.GetContactsResponse;
import com.zimbra.soap.mail.message.SendMsgRequest;
import com.zimbra.soap.mail.type.EmailAddrInfo;
import com.zimbra.soap.mail.type.MsgToSend;
import com.zimbra.soap.type.KeyValuePair;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SendMsgTest {

  private static Account sender;
  private static Account shared;
  private static Account receiver;
  private static GreenMail mta;

  @BeforeAll
  public static void setUp() throws Exception {
    MailboxTestUtil.setUp();
    mta =
        new GreenMail(
            new ServerSetup[] {
              new ServerSetup(
                  SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
            });
    mta.start();
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
    aces.add(new ZimbraACE(
        sender.getId(),
        GranteeType.GT_USER,
        RightManager.getInstance().getRight(Right.RT_sendAs),
        RightModifier.RM_CAN_DELEGATE,
        null));
    final Mailbox sharedMailbox = MailboxManager.getInstance().getMailboxByAccount(shared);

    sharedMailbox.grantAccess(
        null, Mailbox.ID_FOLDER_AUTO_CONTACTS, sender.getId(), ACL.GRANTEE_USER, ACL.stringToRights("rwi"), null);

    ACLUtil.grantRight(Provisioning.getInstance(), shared, aces);
    receiver =
        provisioning.createAccount(
            "rcpt@" + MailboxTestUtil.DEFAULT_DOMAIN,
            "password",
            Maps.newHashMap(Map.of(Provisioning.A_zimbraMailHost, MailboxTestUtil.SERVER_NAME)));
  }

  @AfterAll
  public static void tearDown() throws Exception {
    MailboxTestUtil.tearDown();
    mta.stop();
  }

  @Test
  @DisplayName("Save Draft and send it with some changes -> verify sent and draft deleted")
  void shouldDeleteDraft() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(sender);

    // first, add draft message
    MimeMessage mm = new MimeMessage(JMSession.getSmtpSession(sender));
    mm.setText("foo");
    ParsedMessage pm = new ParsedMessage(mm, false);
    int draftId = mbox.saveDraft(null, pm, Mailbox.ID_AUTO_INCREMENT).getId();

    // then send a message referencing the draft
    Element request = new Element.JSONElement(MailConstants.SEND_MSG_REQUEST);
    Element m =
        request
            .addElement(MailConstants.E_MSG)
            .addAttribute(MailConstants.A_DRAFT_ID, draftId)
            .addAttribute(MailConstants.E_SUBJECT, "dinner appt");
    m.addUniqueElement(MailConstants.E_MIMEPART)
        .addAttribute(MailConstants.A_CONTENT_TYPE, "text/plain")
        .addAttribute(MailConstants.E_CONTENT, "foo bar");
    m.addElement(MailConstants.E_EMAIL)
        .addAttribute(MailConstants.A_ADDRESS_TYPE, ToXML.EmailType.TO.toString())
        .addAttribute(MailConstants.A_ADDRESS, receiver.getName());
    final Element response =
        new SendMsg().handle(request, ServiceTestUtil.getRequestContext(sender));
    // make sure sent message exists
    int sentId =
        (int) response.getElement(MailConstants.E_MSG).getAttributeLong(MailConstants.A_ID);
    Message sent = mbox.getMessageById(null, sentId);
    assertEquals(receiver.getName(), sent.getRecipients());
    Assertions.assertThrows(NoSuchItemException.class, () -> mbox.getMessageById(null, draftId));
  }

  @Test
  @DisplayName("Save draft and send it as is -> verify sends same original data")
  void shouldSendFromDraft() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(sender);

    // first, add draft message
    MimeMessage mm = new MimeMessage(Session.getInstance(new Properties()));
    mm.setRecipients(RecipientType.TO, receiver.getName());
    mm.saveChanges();
    ParsedMessage pm = new ParsedMessage(mm, false);
    int draftId = mbox.saveDraft(null, pm, Mailbox.ID_AUTO_INCREMENT).getId();

    // then send a message referencing the draft
    Element request = new Element.JSONElement(MailConstants.SEND_MSG_REQUEST);
    request
        .addElement(MailConstants.E_MSG)
        .addAttribute(MailConstants.A_DRAFT_ID, draftId)
        .addAttribute(MailConstants.A_SEND_FROM_DRAFT, true);
    Element response = new SendMsg().handle(request, ServiceTestUtil.getRequestContext(sender));

    // make sure sent message exists
    int sentId =
        (int) response.getElement(MailConstants.E_MSG).getAttributeLong(MailConstants.A_ID);
    Message sent = mbox.getMessageById(null, sentId);
    assertEquals(pm.getRecipients(), sent.getRecipients());

    Assertions.assertThrows(NoSuchItemException.class, () -> mbox.getMessageById(null, draftId));
  }

  @Test
  @DisplayName(
      "Upload a file in sender account and send it as email. Verify msg exists in receiver"
          + " mailbox.")
  void shouldSendUploadedFile() throws Exception {
    assertTrue(ZMimeMessage.usingZimbraParser(), "using Zimbra MIME parser");

    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(receiver);

    // Configure test timezones.ics file.
    File tzFile = File.createTempFile("timezones-", ".ics");
    BufferedWriter writer = new BufferedWriter(new FileWriter(tzFile));
    writer.write("BEGIN:VCALENDAR\r\nEND:VCALENDAR");
    writer.close();
    LC.timezone_file.setDefault(tzFile.getAbsolutePath());

    InputStream is = this.getClass().getResourceAsStream("bug-69862-invite.txt");
    ParsedMessage pm = new ParsedMessage(ByteUtil.getContent(is, -1), false);
    mbox.addMessage(null, pm, MailboxTest.STANDARD_DELIVERY_OPTIONS, null);

    is = this.getClass().getResourceAsStream("bug-69862-reply.txt");
    FileUploadServlet.Upload up =
        FileUploadServlet.saveUpload(is, "lslib32.bin", "message/rfc822", sender.getId());

    Element request = new Element.JSONElement(MailConstants.SEND_MSG_REQUEST);
    request
        .addAttribute(MailConstants.A_NEED_CALENDAR_SENTBY_FIXUP, true)
        .addAttribute(MailConstants.A_NO_SAVE_TO_SENT, true);
    request
        .addUniqueElement(MailConstants.E_MSG)
        .addAttribute(MailConstants.A_ATTACHMENT_ID, up.getId());
    new SendMsg().handle(request, ServiceTestUtil.getRequestContext(sender));

    mbox = MailboxManager.getInstance().getMailboxByAccount(receiver);
    Message msg = (Message) mbox.getItemList(null, MailItem.Type.MESSAGE).get(0);
    MimeMessage mm = msg.getMimeMessage();
    assertEquals(
        "multipart/alternative",
        new ZContentType(mm.getContentType()).getBaseType(),
        "correct top-level MIME type");
  }

  @Test
  @DisplayName("Email external user. Check it is added to address book")
  void shouldSaveExternalAddressAsContact() throws Exception {
    final String externalAddress = "external@something.com";
    final SendMsgRequest sendMsgRequest = new SendMsgRequest();
    final MsgToSend msgToSend = new MsgToSend();
    msgToSend.setSubject("Test");
    final EmailAddrInfo rcptAddress = new EmailAddrInfo(externalAddress);
    rcptAddress.setAddressType("t");
    msgToSend.setEmailAddresses(List.of(rcptAddress));
    msgToSend.setContent("Hello there");
    sendMsgRequest.setMsg(msgToSend);
    Element request = JaxbUtil.jaxbToElement(sendMsgRequest);
    new SendMsg().handle(request, ServiceTestUtil.getRequestContext(sender));

    final GetContactsRequest getContactsRequest = new GetContactsRequest();
    final Element handle =
        new GetContacts()
            .handle(
                JaxbUtil.jaxbToElement(getContactsRequest),
                ServiceTestUtil.getRequestContext(sender));
    final GetContactsResponse getContactsResponse =
        JaxbUtil.elementToJaxb(handle, GetContactsResponse.class);
    // Believe or not, ContactInfo email is null but "email" attribute is not, so we have to get it
    // from there
    final List<String> contacts =
        getContactsResponse.getContacts().stream()
            .flatMap(
                contactInfo ->
                    contactInfo.getAttrs().stream()
                        .filter(attr -> attr.getKey().equals("email"))
                        .map(KeyValuePair::getValue))
            .collect(Collectors.toList());
    Assertions.assertArrayEquals(List.of(externalAddress).toArray(), contacts.toArray());
  }

  @Test
  @DisplayName("Email external user as shared account. Check it is added to shared account address book")
  void shouldSaveExternalAddressAsContactInSharedAccount() throws Exception {
    final String externalAddress = "external@something.com";
    final SendMsgRequest sendMsgRequest = new SendMsgRequest();
    final MsgToSend msgToSend = new MsgToSend();
    msgToSend.setSubject("Test");
    msgToSend.setIdentityId(shared.getId());
    final EmailAddrInfo rcptAddress = new EmailAddrInfo(externalAddress);
    rcptAddress.setAddressType("t");
    msgToSend.setEmailAddresses(List.of(rcptAddress));
    msgToSend.setContent("Hello there");
    sendMsgRequest.setMsg(msgToSend);
    Map<String, Object> sharedCtx = new HashMap<String, Object>();
    ZimbraSoapContext zsc =
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(sender),
            shared.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    sharedCtx.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    Element request = JaxbUtil.jaxbToElement(sendMsgRequest);
    new SendMsg().handle(request, sharedCtx);

    final GetContactsRequest getContactsRequest = new GetContactsRequest();
    final GetContactsResponse getContactsResponse =
        JaxbUtil.elementToJaxb(new GetContacts()
            .handle(
                JaxbUtil.jaxbToElement(getContactsRequest), ServiceTestUtil.getRequestContext(sender)), GetContactsResponse.class);
    Assertions.assertEquals(0, getContactsResponse.getContacts().size());
    final GetContactsResponse sharedAccountContacts =
        JaxbUtil.elementToJaxb(new GetContacts()
            .handle(
                JaxbUtil.jaxbToElement(getContactsRequest), ServiceTestUtil.getRequestContext(shared)), GetContactsResponse.class);
    Assertions.assertEquals(1, sharedAccountContacts.getContacts().size());

  }
}

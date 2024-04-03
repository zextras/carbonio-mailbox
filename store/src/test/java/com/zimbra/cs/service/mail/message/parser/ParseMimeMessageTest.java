// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail.message.parser;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.util.MailMessageBuilder;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.zmime.ZMimeMultipart;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.cs.service.mail.ToXML.EmailType;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.mail.message.SaveDraftRequest;
import com.zimbra.soap.mail.type.AttachSpec;
import com.zimbra.soap.mail.type.AttachmentsInfo;
import com.zimbra.soap.mail.type.MimePartAttachSpec;
import com.zimbra.soap.mail.type.SaveDraftMsg;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.dom4j.QName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class ParseMimeMessageTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

 @Test
 void parseMimeMsgSoap() throws Exception {
  Element rootEl = new Element.JSONElement(MailConstants.E_SEND_MSG_REQUEST);
  Element el = new Element.JSONElement(MailConstants.E_MSG);
   el.addAttribute(MailConstants.E_SUBJECT, "dinner appt");
   el.addUniqueElement(MailConstants.E_MIMEPART)
    .addAttribute(MailConstants.A_CONTENT_TYPE, "text/plain")
    .addAttribute(MailConstants.E_CONTENT, "foo bar");
   el.addElement(MailConstants.E_EMAIL)
    .addAttribute(MailConstants.A_ADDRESS_TYPE, EmailType.TO.toString())
    .addAttribute(MailConstants.A_ADDRESS, "rcpt@zimbra.com");

   rootEl.addUniqueElement(el);
   Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
   OperationContext octxt = new OperationContext(acct);
  ZimbraSoapContext zsc = getMockSoapContext();

  MimeMessage mm =
    ParseMimeMessage.parseMimeMsgSoap(
      zsc, octxt, null, el, new MimeMessageData());
  assertEquals("text/plain; charset=utf-8", mm.getContentType());
  assertEquals("dinner appt", mm.getSubject());
  assertEquals("rcpt@zimbra.com", mm.getHeader("To", ","));
  assertEquals("7bit", mm.getHeader("Content-Transfer-Encoding", ","));
  assertEquals("foo bar", mm.getContent());
 }

  @Test
  void parseMimeMsgSoapWithEmptyFile_DoesNotAffectMtaMaxMessageSizeQuotaCheck() throws Exception {
    Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
    OperationContext octxt = new OperationContext(account);
    ZimbraSoapContext parent =
        new ZimbraSoapContext(
            (Element) null,
            (QName) null,
            (DocumentHandler) null,
            Collections.<String, Object>emptyMap(),
            SoapProtocol.SoapJS);
    ZimbraSoapContext zsc = new ZimbraSoapContext(parent, new ZimbraAuthToken(account), MockProvisioning.DEFAULT_ACCOUNT_ID, null);
    final Message draftWithFileAttachment = this.createDraftWithFileAttachment(account);

    // upload empty file
    final InputStream uploadInputStream = this.getClass()
        .getResourceAsStream("/empty-file.txt");
    final String filename = "emptyFile.txt";
    final Upload upload = FileUploadServlet.saveUpload(uploadInputStream, filename,
        "text/plain", account.getId(), true);

    final SaveDraftRequest sendMsgRequest = new SaveDraftRequest();
    final SaveDraftMsg msgToSend = new SaveDraftMsg();
    final int draftId = draftWithFileAttachment.getId();
    msgToSend.setId(draftId);
    msgToSend.setContent("Hey there!");
    msgToSend.setSubject("Test subject");
    final AttachmentsInfo attachmentsInfo = new AttachmentsInfo();
    attachmentsInfo.setAttachmentId(upload.getId());
    msgToSend.setAttachments(attachmentsInfo);
    sendMsgRequest.setMsg(msgToSend);
    final Element rootElem = JaxbUtil.jaxbToElement(sendMsgRequest);
    final Element msgElement = rootElem.getElement(MailConstants.E_MSG);

    final MimeMessage mimeMessage = ParseMimeMessage.parseDraftMimeMsgSoap(zsc, octxt, null,
        msgElement, new MimeMessageData());

    final ZMimeMultipart multiPart = getMimeMultiPart(mimeMessage);
    Assertions.assertEquals (1, multiPart.getCount());
    final BodyPart attachment = multiPart.getBodyPart(0);
    Assertions.assertEquals ("", attachment.getContent());
    Assertions.assertEquals (filename, attachment.getFileName());
  }

  @Test
  void parseMimeMsgSoapWithSmartLink() throws Exception {
    Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
    OperationContext octxt = new OperationContext(account);
    ZimbraSoapContext parent =
        new ZimbraSoapContext(
            (Element) null,
            (QName) null,
            (DocumentHandler) null,
            Collections.<String, Object>emptyMap(),
            SoapProtocol.SoapJS);
    ZimbraSoapContext zsc = new ZimbraSoapContext(parent, new ZimbraAuthToken(account), MockProvisioning.DEFAULT_ACCOUNT_ID, null);

    // 1 save a draft message with attachments
    final Message draftWithFileAttachment = this.createDraftWithFileAttachment(account);

    // set attachment as smartlink
    final SaveDraftRequest sendMsgRequest = new SaveDraftRequest();
    final SaveDraftMsg msgToSend = new SaveDraftMsg();
    final int draftId = draftWithFileAttachment.getId();
    msgToSend.setId(draftId);
    msgToSend.setContent("Hey there!");
    msgToSend.setSubject("Test subject");
    final AttachmentsInfo attachmentsInfo = new AttachmentsInfo();
    final ArrayList<AttachSpec> attachments = new ArrayList<>();
    attachments.add(new MimePartAttachSpec(String.valueOf(draftId), "2", true));
    attachmentsInfo.setAttachments(attachments);
    msgToSend.setAttachments(attachmentsInfo);
    sendMsgRequest.setMsg(msgToSend);
    final Element rootElem = JaxbUtil.jaxbToElement(sendMsgRequest);
    final Element msgElement = rootElem.getElement(MailConstants.E_MSG);

    MimeMessage mimeMessageWithAttachment =
        ParseMimeMessage.parseMimeMsgSoap(
            zsc, octxt, null, msgElement, new MimeMessageData());
    mimeMessageWithAttachment.getContent();
    final String[] header = getMimeMultiPart(mimeMessageWithAttachment).getBodyPart(0)
        .getHeader(ParseMimeMessage.SMART_LINK_HEADER);
    Assertions.assertNotNull(header);
    Assertions.assertTrue(header.length > 0);
  }

  @Test
  void parseMimeMsgSoapWithNewAttachmentShouldNotAddSmartLink() throws Exception {
    Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
    OperationContext octxt = new OperationContext(account);
    ZimbraSoapContext parent =
        new ZimbraSoapContext(
            (Element) null,
            (QName) null,
            (DocumentHandler) null,
            Collections.<String, Object>emptyMap(),
            SoapProtocol.SoapJS);
    ZimbraSoapContext zsc = new ZimbraSoapContext(parent, new ZimbraAuthToken(account), MockProvisioning.DEFAULT_ACCOUNT_ID, null);

    // 1 save a draft message with attachments
    final Message draft = this.createDraft(account);

    // 2 upload file
    final InputStream uploadInputStream = this.getClass()
        .getResourceAsStream("/test-save-to-files.txt");
    final Upload upload = FileUploadServlet.saveUpload(uploadInputStream, "myFiletest.txt",
        "text/plain", account.getId());

    // set attachment as smartlink
    final SaveDraftRequest sendMsgRequest = new SaveDraftRequest();
    final SaveDraftMsg msgToSend = new SaveDraftMsg();
    final int draftId = draft.getId();
    msgToSend.setId(draftId);
    msgToSend.setContent("Hey there!");
    msgToSend.setSubject("Test subject");
    final AttachmentsInfo attachmentsInfo = new AttachmentsInfo();
    attachmentsInfo.setAttachmentId(upload.getId());
    msgToSend.setAttachments(attachmentsInfo);
    sendMsgRequest.setMsg(msgToSend);
    final Element rootElem = JaxbUtil.jaxbToElement(sendMsgRequest);
    final Element msgElement = rootElem.getElement(MailConstants.E_MSG);

    MimeMessage mimeMessageWithAttachment =
        ParseMimeMessage.parseMimeMsgSoap(
            zsc, octxt, null, msgElement, new MimeMessageData());
    mimeMessageWithAttachment.getContent();
    final String[] header = getMimeMultiPart(mimeMessageWithAttachment).getBodyPart(0)
        .getHeader(ParseMimeMessage.SMART_LINK_HEADER);
    Assertions.assertNull(header);
  }


  @Test
  void parseMimeMsgSoap_IgnoresFileSizeMtaQuotaWhenNoAttachments() throws Exception {
    var config = Provisioning.getInstance().getConfig();
    try {
      config.modify(new HashMap<>(Map.of(
          Provisioning.A_zimbraMtaMaxMessageSize, "1"
      )));

      Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
      OperationContext octxt = new OperationContext(account);
      ZimbraSoapContext parent =
          new ZimbraSoapContext(
              (Element) null,
              (QName) null,
              (DocumentHandler) null,
              Collections.<String, Object>emptyMap(),
              SoapProtocol.SoapJS);
      ZimbraSoapContext zsc = new ZimbraSoapContext(parent, new ZimbraAuthToken(account), MockProvisioning.DEFAULT_ACCOUNT_ID, null);

      // 1 save a draft message with attachments
      final Message draft = this.createDraft(account);

      // set attachment as smartlink
      final SaveDraftRequest sendMsgRequest = new SaveDraftRequest();
      final SaveDraftMsg msgToSend = new SaveDraftMsg();
      final int draftId = draft.getId();
      msgToSend.setId(draftId);
      msgToSend.setContent("Hey there!");
      msgToSend.setSubject("Test subject");
      sendMsgRequest.setMsg(msgToSend);
      final Element rootElem = JaxbUtil.jaxbToElement(sendMsgRequest);
      final Element msgElement = rootElem.getElement(MailConstants.E_MSG);

      Assertions.assertDoesNotThrow( () -> {
        ParseMimeMessage.parseMimeMsgSoap(
            zsc, octxt, null, msgElement, new MimeMessageData());
      });

    } finally {
      config.unsetMtaMaxMessageSize();
    }
  }

  @Test
  void parseMimeMsgSoap_WithFileSizeExceedingMtaQuotaFails() throws Exception {
    var config = Provisioning.getInstance().getConfig();
    try {
      config.modify(new HashMap<>(Map.of(
          Provisioning.A_zimbraMtaMaxMessageSize, "1"
      )));

      Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
      OperationContext octxt = new OperationContext(account);
      ZimbraSoapContext parent =
          new ZimbraSoapContext(
              (Element) null,
              (QName) null,
              (DocumentHandler) null,
              Collections.<String, Object>emptyMap(),
              SoapProtocol.SoapJS);
      ZimbraSoapContext zsc = new ZimbraSoapContext(parent, new ZimbraAuthToken(account), MockProvisioning.DEFAULT_ACCOUNT_ID, null);

      // 1 save a draft message with attachments
      final Message draft = this.createDraft(account);

      // 2 upload file
      final InputStream uploadInputStream = this.getClass()
          .getResourceAsStream("/test-save-to-files.txt");
      final Upload upload = FileUploadServlet.saveUpload(uploadInputStream, "myFiletest.txt",
          "text/plain", account.getId(), true);

      // set attachment as smartlink
      final SaveDraftRequest sendMsgRequest = new SaveDraftRequest();
      final SaveDraftMsg msgToSend = new SaveDraftMsg();
      final int draftId = draft.getId();
      msgToSend.setId(draftId);
      msgToSend.setContent("Hey there!");
      msgToSend.setSubject("Test subject");
      final AttachmentsInfo attachmentsInfo = new AttachmentsInfo();
      attachmentsInfo.setAttachmentId(upload.getId());
      msgToSend.setAttachments(attachmentsInfo);
      sendMsgRequest.setMsg(msgToSend);
      final Element rootElem = JaxbUtil.jaxbToElement(sendMsgRequest);
      final Element msgElement = rootElem.getElement(MailConstants.E_MSG);

      Assertions.assertThrows( MailServiceException.class, () -> {
        ParseMimeMessage.parseMimeMsgSoap(
            zsc, octxt, null, msgElement, new MimeMessageData());
          });

    } finally {
      config.unsetMtaMaxMessageSize();
    }
  }


  @Test
  void parseDraftMimeMsgSoap() throws Exception {
    Element rootEl = new Element.JSONElement(MailConstants.E_SEND_MSG_REQUEST);
    Element msgElement = new Element.JSONElement(MailConstants.E_MSG);
    msgElement.addAttribute(MailConstants.E_SUBJECT, "dinner appt");
    msgElement.addUniqueElement(MailConstants.E_MIMEPART)
        .addAttribute(MailConstants.A_CONTENT_TYPE, "text/plain")
        .addAttribute(MailConstants.E_CONTENT, "foo bar");
    msgElement.addElement(MailConstants.E_EMAIL)
        .addAttribute(MailConstants.A_ADDRESS_TYPE, EmailType.TO.toString())
        .addAttribute(MailConstants.A_ADDRESS, "rcpt@zimbra.com");

    rootEl.addUniqueElement(msgElement);
    Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
    OperationContext octxt = new OperationContext(acct);
    ZimbraSoapContext zsc = getMockSoapContext();

    MimeMessage mm =
        ParseMimeMessage.parseDraftMimeMsgSoap(
            zsc, octxt, null, msgElement, new MimeMessageData());
    assertEquals("text/plain; charset=utf-8", mm.getContentType());
    assertEquals("dinner appt", mm.getSubject());
    assertEquals("rcpt@zimbra.com", mm.getHeader("To", ","));
    assertEquals("7bit", mm.getHeader("Content-Transfer-Encoding", ","));
    assertEquals("foo bar", mm.getContent());
  }

  @Test
  void parseDraftMimeMsgSoap_addNewInlineAttachment() throws Exception {
    Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);

    final InputStream uploadInputStream = this.getClass()
        .getResourceAsStream("/test-save-to-files.txt");
    final String filename = "myFiletest.txt";
    final Upload upload = FileUploadServlet.saveUpload(uploadInputStream, filename,
        "text/plain", account.getId(), true);

    Element request = new Element.JSONElement(MailConstants.E_SAVE_DRAFT_REQUEST);
    Element msgElement = new Element.JSONElement(MailConstants.E_MSG);
    msgElement.addAttribute(MailConstants.E_SUBJECT, "dinner appt");
    final String contentId = upload.getId() + "@carbonio";
    final Element multiPart = msgElement.addUniqueElement(MailConstants.E_MIMEPART)
        .addAttribute(MailConstants.A_CONTENT_TYPE, "multipart/alternative");
    final Element multiPartRelated = multiPart.addUniqueElement(MailConstants.E_MIMEPART)
        .addAttribute(MailConstants.A_CONTENT_TYPE, "multipart/related");
    final Element inlineAttachment = multiPartRelated.addUniqueElement(MailConstants.E_MIMEPART)
        .addAttribute(MailConstants.A_CONTENT_ID, contentId)
        .addAttribute(MailConstants.A_CONTENT_DISPOSITION, "inline");
    inlineAttachment.addUniqueElement(MailConstants.E_ATTACH)
        .addAttribute(MailConstants.A_ATTACHMENT_ID, upload.getId());

    msgElement.addElement(MailConstants.E_EMAIL)
        .addAttribute(MailConstants.A_ADDRESS_TYPE, EmailType.TO.toString())
        .addAttribute(MailConstants.A_ADDRESS, "rcpt@zimbra.com");
    request.addUniqueElement(msgElement);
    OperationContext octxt = new OperationContext(account);
    ZimbraSoapContext zsc = getZimbraSoapContext(account);

    MimeMessage mm = ParseMimeMessage.parseDraftMimeMsgSoap(zsc, octxt, null, msgElement, new MimeMessageData());

    final BodyPart newAttachment =  getMimeMultiPart(getMimeMultiPart(mm).getBodyPart(0)).getBodyPart(0);
    assertEquals(filename, newAttachment.getFileName());
    assertEquals("inline; filename=" + filename, newAttachment.getHeader("Content-Disposition")[0]);
    assertEquals("<" + contentId + ">", newAttachment.getHeader("Content-ID")[0]);
    assertEquals("dinner appt", mm.getSubject());
    assertEquals("rcpt@zimbra.com", mm.getHeader("To", ","));
    assertNull(mm.getHeader("Content-Transfer-Encoding", ","));
  }


  @Test
  void parseDraftMimeMsgSoap_SucceedsEvenIfSizeExceedsMtaQuota() throws Exception {
    var config = Provisioning.getInstance().getConfig();
    try {
      config.modify(new HashMap<>(Map.of(
          Provisioning.A_zimbraMtaMaxMessageSize, "1"
      )));

      Account account = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
      OperationContext octxt = new OperationContext(account);
      ZimbraSoapContext zsc = getZimbraSoapContext(account);

      // 1 save a draft message with attachments
      final Message draft = this.createDraft(account);

      // 2 upload file
      final InputStream uploadInputStream = this.getClass()
          .getResourceAsStream("/test-save-to-files.txt");
      final Upload upload = FileUploadServlet.saveUpload(uploadInputStream, "myFiletest.txt",
          "text/plain", account.getId(), true);

      // set attachment as smartlink
      final SaveDraftRequest sendMsgRequest = new SaveDraftRequest();
      final SaveDraftMsg msgToSend = new SaveDraftMsg();
      final int draftId = draft.getId();
      msgToSend.setId(draftId);
      msgToSend.setContent("Hey there!");
      msgToSend.setSubject("Test subject");
      final AttachmentsInfo attachmentsInfo = new AttachmentsInfo();
      attachmentsInfo.setAttachmentId(upload.getId());
      msgToSend.setAttachments(attachmentsInfo);
      sendMsgRequest.setMsg(msgToSend);
      final Element rootElem = JaxbUtil.jaxbToElement(sendMsgRequest);
      final Element msgElement = rootElem.getElement(MailConstants.E_MSG);

      Assertions.assertDoesNotThrow( () -> {
        ParseMimeMessage.parseDraftMimeMsgSoap(
            zsc, octxt, null, msgElement, new MimeMessageData());
      });

    } finally {
      config.unsetMtaMaxMessageSize();
    }
  }

 @Test
 void customMimeHeader() throws Exception {
    Element rootEl = new Element.JSONElement(MailConstants.E_SEND_MSG_REQUEST);
  Element el = new Element.JSONElement(MailConstants.E_MSG);
  el.addAttribute(MailConstants.E_SUBJECT, "subject");
  el.addUniqueElement(MailConstants.E_MIMEPART)
    .addAttribute(MailConstants.A_CONTENT_TYPE, "text/plain")
    .addAttribute(MailConstants.E_CONTENT, "body");
  el.addElement(MailConstants.E_EMAIL)
    .addAttribute(MailConstants.A_ADDRESS_TYPE, EmailType.TO.toString())
    .addAttribute(MailConstants.A_ADDRESS, "rcpt@zimbra.com");
  el.addElement(MailConstants.E_HEADER)
    .addAttribute(MailConstants.A_NAME, "X-Zimbra-Test")
    .setText("custom");
  el.addElement(MailConstants.E_HEADER)
    .addAttribute(MailConstants.A_NAME, "X-Zimbra-Test")
    .setText("\u30ab\u30b9\u30bf\u30e0");
  rootEl.addUniqueElement(el);

  Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
  OperationContext octxt = new OperationContext(acct);
  ZimbraSoapContext zsc = getMockSoapContext();

  MimeMessage mm;
  try {
   mm =
     ParseMimeMessage.parseMimeMsgSoap(
       zsc, octxt, null, el, new MimeMessageData());
   fail();
  } catch (ServiceException expected) {
   assertEquals("invalid request: header 'X-Zimbra-Test' not allowed", expected.getMessage());
  }

  Provisioning.getInstance()
    .getConfig()
    .setCustomMimeHeaderNameAllowed(new String[]{"X-Zimbra-Test"});
  mm =
    ParseMimeMessage.parseMimeMsgSoap(
      zsc, octxt, null, el, new MimeMessageData());
  assertEquals("custom, =?utf-8?B?44Kr44K544K/44Og?=", mm.getHeader("X-Zimbra-Test", ", "));
 }

 @Test
 void attachedMessage() throws Exception {
   Element rootEl = new Element.JSONElement(MailConstants.E_SEND_MSG_REQUEST);
  Element el = new Element.JSONElement(MailConstants.E_MSG);
  el.addAttribute(MailConstants.E_SUBJECT, "attach message");
  el.addElement(MailConstants.E_EMAIL)
    .addAttribute(MailConstants.A_ADDRESS_TYPE, EmailType.TO.toString())
    .addAttribute(MailConstants.A_ADDRESS, "rcpt@zimbra.com");
  Element mp =
    el.addUniqueElement(MailConstants.E_MIMEPART)
      .addAttribute(MailConstants.A_CONTENT_TYPE, "multipart/mixed;");
  mp.addElement(MailConstants.E_MIMEPART)
    .addAttribute(MailConstants.A_CONTENT_TYPE, "text/plain")
    .addAttribute(MailConstants.E_CONTENT, "This is the outer message.");
  mp.addElement(MailConstants.E_MIMEPART)
    .addAttribute(MailConstants.A_CONTENT_TYPE, "message/rfc822")
    .addAttribute(
      MailConstants.E_CONTENT,
      "From: inner-sender@zimbra.com\r\n"
        + "To: inner-rcpt@zimbra.com\r\n"
        + "Subject: inner-message\r\n"
        + "Content-Type: text/plain\r\n"
        + "Content-Transfer-Encoding: 7bit\r\n"
        + "MIME-Version: 1.0\r\n\r\n"
        + "This is the inner message.");
  rootEl.addUniqueElement(el);

  Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
  OperationContext octxt = new OperationContext(acct);
  ZimbraSoapContext zsc = getMockSoapContext();

  MimeMessage mm =
    ParseMimeMessage.parseMimeMsgSoap(
      zsc, octxt, null, el, new MimeMessageData());
  assertTrue(mm.getContentType().startsWith("multipart/mixed;"));
  assertEquals("attach message", mm.getSubject());
  assertEquals("rcpt@zimbra.com", mm.getHeader("To", ","));
  MimeMultipart mmp = (MimeMultipart) mm.getContent();
  assertEquals(2, mmp.getCount());
  assertTrue(mmp.getContentType().startsWith("multipart/mixed;"));

  MimeBodyPart part = (MimeBodyPart) mmp.getBodyPart(0);
  assertEquals("text/plain; charset=utf-8", part.getContentType());
  assertEquals("7bit", part.getHeader("Content-Transfer-Encoding", ","));
  assertEquals("This is the outer message.", part.getContent());

  part = (MimeBodyPart) mmp.getBodyPart(1);
  assertEquals("message/rfc822; charset=utf-8", part.getContentType());
  MimeMessage msg = (MimeMessage) part.getContent();
  assertEquals("text/plain", msg.getContentType());
  assertEquals("inner-message", msg.getSubject());
  assertEquals("This is the inner message.", msg.getContent());
 }

 @Test
 void shouldReturnElementWithGivenContentTypeWhenCalledGetFirstElementFromMimePartByType()
   throws ServiceException {
  // Setup
  final Element jsonElement = new Element.JSONElement(MailConstants.E_MSG);
  jsonElement.addAttribute(MailConstants.E_SUBJECT, "subject");

  final Element multiMimePart =
    jsonElement
      .addUniqueElement(MailConstants.E_MIMEPART)
      .addAttribute(MailConstants.A_CONTENT_TYPE, "multipart/alternative");

  multiMimePart
    .addNonUniqueElement(MailConstants.E_MIMEPART)
    .addAttribute(MailConstants.A_CONTENT_TYPE, "text/html")
    .addAttribute(MailConstants.E_CONTENT, "foo");

  multiMimePart
    .addNonUniqueElement(MailConstants.E_MIMEPART)
    .addAttribute(MailConstants.A_CONTENT_TYPE, "text/plain")
    .addAttribute(MailConstants.E_CONTENT, "loo");

  multiMimePart
    .addNonUniqueElement(MailConstants.E_MIMEPART)
    .addAttribute(MailConstants.A_CONTENT_TYPE, "text/svg+xml")
    .addAttribute(MailConstants.E_CONTENT, "too");

  // Execute, first element with MimeConstants.CT_TEXT_PLAIN ContentType
  final Optional<Element> expectedFirstTextMimePart =
    ParseMimeMessage.getFirstElementFromMimePartByType(
      jsonElement, MimeConstants.CT_TEXT_PLAIN);

  // Verify, MimeConstants.CT_TEXT_PLAIN
  assertTrue(expectedFirstTextMimePart.isPresent());
  assertTrue(
    multiMimePart
      .listElements(MailConstants.E_MIMEPART)
      .contains(expectedFirstTextMimePart.get()));
  assertEquals("loo", expectedFirstTextMimePart.get().getAttribute(MailConstants.E_CONTENT));

  // Execute, first element with MimeConstants.CT_TEXT_HTML ContentType
  final Optional<Element> expectedFirstTextHtmlMimePart =
    ParseMimeMessage.getFirstElementFromMimePartByType(jsonElement, MimeConstants.CT_TEXT_HTML);

  // Verify, MimeConstants.CT_TEXT_HTML
  assertTrue(expectedFirstTextHtmlMimePart.isPresent());
  assertTrue(
    multiMimePart
      .listElements(MailConstants.E_MIMEPART)
      .contains(expectedFirstTextHtmlMimePart.get()));
  assertEquals("foo", expectedFirstTextHtmlMimePart.get().getAttribute(MailConstants.E_CONTENT));
 }


  private Message createDraftWithFileAttachment(Account account) throws Exception {
    final String accountName = account.getName();
    final ParsedMessage message = new MailMessageBuilder()
        .from(accountName)
        .addRecipient(accountName)
        .subject("Test email")
        .body("Hello there")
        .addAttachmentFromResources("/test-save-to-files.txt")
        .build();
    return AccountAction.Factory.getDefault().forAccount(account).saveDraft(message);
  }

  private Message createDraft(Account account) throws Exception {
    final String accountName = account.getName();
    final ParsedMessage message = new MailMessageBuilder()
        .from(accountName)
        .addRecipient(accountName)
        .subject("Test email")
        .body("Hello there")
        .build();
    return AccountAction.Factory.getDefault().forAccount(account).saveDraft(message);
  }


  private static ZimbraSoapContext getMockSoapContext() throws ServiceException {
    ZimbraSoapContext parent =
        new ZimbraSoapContext(
            (Element) null,
            (QName) null,
            (DocumentHandler) null,
            Collections.<String, Object>emptyMap(),
            SoapProtocol.SoapJS);
    return new ZimbraSoapContext(parent, MockProvisioning.DEFAULT_ACCOUNT_ID, null);
  }

  private ZimbraSoapContext getZimbraSoapContext(Account account) throws ServiceException {
    ZimbraSoapContext parent =
        new ZimbraSoapContext(
            (Element) null,
            (QName) null,
            (DocumentHandler) null,
            Collections.<String, Object>emptyMap(),
            SoapProtocol.SoapJS);
    return new ZimbraSoapContext(parent, new ZimbraAuthToken(account), account.getId(), null);
  }

  private ZMimeMultipart getMimeMultiPart(MimeMessage mm) throws IOException, MessagingException {
    return (ZMimeMultipart) mm.getContent();
  }

  private ZMimeMultipart getMimeMultiPart(BodyPart bodyPart) throws IOException, MessagingException {
    return (ZMimeMultipart) bodyPart.getContent();
  }
}

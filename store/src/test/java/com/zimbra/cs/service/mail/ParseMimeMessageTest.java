// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.zmime.ZMimeUtility;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.mail.ToXML.EmailType;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import org.dom4j.QName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ParseMimeMessage}.
 *
 * @author ysasaki
 */
public final class ParseMimeMessageTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  static ZimbraSoapContext getMockSoapContext() throws ServiceException {
    ZimbraSoapContext parent =
        new ZimbraSoapContext(
            (Element) null,
            (QName) null,
            (DocumentHandler) null,
            Collections.<String, Object>emptyMap(),
            SoapProtocol.SoapJS);
    return new ZimbraSoapContext(parent, MockProvisioning.DEFAULT_ACCOUNT_ID, null);
  }

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

 @Test
 @Disabled("Fix me. Null pointer exception.")
 void parseMimeMsgSoap() throws Exception {
  Element el = new Element.JSONElement(MailConstants.E_MSG);
  el.addAttribute(MailConstants.E_SUBJECT, "dinner appt");
  el.addUniqueElement(MailConstants.E_MIMEPART)
    .addAttribute(MailConstants.A_CONTENT_TYPE, "text/plain")
    .addAttribute(MailConstants.E_CONTENT, "foo bar");
  el.addElement(MailConstants.E_EMAIL)
    .addAttribute(MailConstants.A_ADDRESS_TYPE, EmailType.TO.toString())
    .addAttribute(MailConstants.A_ADDRESS, "rcpt@zimbra.com");

  Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
  OperationContext octxt = new OperationContext(acct);
  ZimbraSoapContext zsc = getMockSoapContext();

  MimeMessage mm =
    ParseMimeMessage.parseMimeMsgSoap(
      zsc, octxt, null, el, null, new ParseMimeMessage.MimeMessageData());
  assertEquals("text/plain; charset=utf-8", mm.getContentType());
  assertEquals("dinner appt", mm.getSubject());
  assertEquals("rcpt@zimbra.com", mm.getHeader("To", ","));
  assertEquals("7bit", mm.getHeader("Content-Transfer-Encoding", ","));
  assertEquals("foo bar", mm.getContent());
 }

 @Test
 @Disabled("Fix me. Null pointer exception.")
 void customMimeHeader() throws Exception {
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

  Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
  OperationContext octxt = new OperationContext(acct);
  ZimbraSoapContext zsc = getMockSoapContext();

  MimeMessage mm;
  try {
   mm =
     ParseMimeMessage.parseMimeMsgSoap(
       zsc, octxt, null, el, null, new ParseMimeMessage.MimeMessageData());
   fail();
  } catch (ServiceException expected) {
   assertEquals("invalid request: header 'X-Zimbra-Test' not allowed", expected.getMessage());
  }

  Provisioning.getInstance()
    .getConfig()
    .setCustomMimeHeaderNameAllowed(new String[]{"X-Zimbra-Test"});
  mm =
    ParseMimeMessage.parseMimeMsgSoap(
      zsc, octxt, null, el, null, new ParseMimeMessage.MimeMessageData());
  assertEquals("custom, =?utf-8?B?44Kr44K544K/44Og?=", mm.getHeader("X-Zimbra-Test", ", "));
 }

 @Test
 @Disabled("Fix me. Null pointer exception.")
 void attachedMessage() throws Exception {
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

  Account acct = Provisioning.getInstance().getAccount(MockProvisioning.DEFAULT_ACCOUNT_ID);
  OperationContext octxt = new OperationContext(acct);
  ZimbraSoapContext zsc = getMockSoapContext();

  MimeMessage mm =
    ParseMimeMessage.parseMimeMsgSoap(
      zsc, octxt, null, el, null, new ParseMimeMessage.MimeMessageData());
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

  private ByteArrayInputStream randomContent(String prefix, int length) {
    ZMimeUtility.ByteBuilder bb = new ZMimeUtility.ByteBuilder();
    Random rnd = new Random();
    bb.append(prefix).append("\n");
    for (int i = prefix.length() + 2; i < length; i++) {
      int r = rnd.nextInt(55);
      if (r < 26) {
        bb.append((char) ('A' + r));
      } else if (r < 52) {
        bb.append((char) ('a' + r));
      } else {
        bb.append(' ');
      }
    }
    return new ByteArrayInputStream(bb.toByteArray());
  }

  private String firstLine(MimePart part) throws IOException, MessagingException {
    return new BufferedReader(new InputStreamReader(part.getInputStream())).readLine();
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
}

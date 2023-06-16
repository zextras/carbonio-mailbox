// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Sets;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.util.JMSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Set;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import javax.mail.util.SharedByteArrayInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import qa.unittest.TestUtil;

/**
 * Unit test for {@link Mime}.
 *
 * @author ysasaki
 */
public class MimeTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  private void testCP932(String contentType) throws IOException {
    Reader reader =
        Mime.getTextReader(getClass().getResourceAsStream("cp932.txt"), contentType, null);
    String result = IOUtils.toString(reader);
   assertEquals(result, "2010/4/2,\u2161\u53f7\u5e97 "
     + " \u30ab\u30aa\u30b9\u9928,\u3054\u672c\u4eba,1\u56de\u6255\u3044,,'10/05,9960,9960,,,,," + System.lineSeparator());
  }

 @Test
 void getTextReader() throws Exception {
  Reader reader =
    Mime.getTextReader(getClass().getResourceAsStream("shift-jis.txt"), "text/plain", null);
  String result = IOUtils.toString(reader);
  assertTrue(
    result.startsWith(
      "Zimbra Collaboration Suite\uff08ZCS\uff09\u306f\u3001Zimbra, Inc. "
        + "\u304c\u958b\u767a\u3057\u305f\u30b3\u30e9\u30dc\u30ec\u30fc\u30b7\u30e7\u30f3\u30bd\u30d5\u30c8"
        + "\u30a6\u30a7\u30a2\u88fd\u54c1\u3002"));
  assertTrue(
    result.endsWith(
      "\u65e5\u672c\u3067\u306f\u4f4f\u53cb\u5546\u4e8b\u304c\u7dcf\u8ca9\u58f2"
        + "\u4ee3\u7406\u5e97\u3068\u306a\u3063\u3066\u3044\u308b\u3002"));

  // ICU4J thinks it's UTF-32 with confidence 25. We only trust if the
  // confidence is greater than 50.
  reader =
    Mime.getTextReader(
      getClass().getResourceAsStream("p4-notification.txt"), "text/plain", null);
  result = IOUtils.toString(reader);
  assertTrue(result.startsWith("Change 259706"));

  testCP932("text/plain");
  testCP932("text/plain; charset=shift_jis");
  testCP932("text/plain; charset=windows-31j");
  testCP932("text/plain; charset=cp932");
 }

 @Test
 void parseAddressHeader() throws Exception {
  InternetAddress[] addrs = Mime.parseAddressHeader("\" <test@zimbra.com>");
  assertEquals(1, addrs.length);
  // Only verify an exception is not thrown. The new parser and the old
  // parser don't get the same result.
 }

 @Test
 void multipartReport() throws Exception {
  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), getClass().getResourceAsStream("NDR.txt"));
  List<MPartInfo> parts = Mime.getParts(mm);
  assertNotNull(parts);
  assertEquals(8, parts.size());

  Set<MPartInfo> bodies = Mime.getBody(parts, true);
  assertEquals(2, bodies.size());
  boolean foundFirstBody = false;
  boolean foundSecondBody = false;
  for (MPartInfo body : bodies) {
   Object content = body.getMimePart().getContent();
   assertTrue(content instanceof String);
   String text = (String) content;
   if (body.getPartNum() == 1 && text.startsWith("This is an automated message")) {
    foundFirstBody = true;
   } else if (body.getPartNum() == 2 && text.startsWith("<badaddress@gmailllllll.com>")) {
    foundSecondBody = true;
   }
  }

  assertTrue(foundFirstBody, "first body found");
  assertTrue(foundSecondBody, "second body found");
 }

  String boundary = "----------1111971890AC3BB91";

  String baseNdrContent =
      "From: MAILER-DAEMON@example.com\r\n"
          + "To: user2@example.com\r\n"
          + "Subject: Postmaster Copy: Undelivered Mail\r\n"
          + "Content-Type: multipart/report; report-type=delivery-status;\r\n"
          + "boundary=\""
          + boundary
          + "\"\r\n";

 @Test
 void multipartReportRfc822Headers() throws Exception {
  String content =
    baseNdrContent
      + boundary
      + "\r\n"
      + "Content-Description: Notification\r\n"
      + "Content-Type: text/plain;charset=us-ascii\r\n\r\n"
      + "<mta@example.com>: host mta.example.com[255.255.255.0] said: 554 delivery error:"
      + " This user doesn't have an example.com account (in reply to end of DATA"
      + " command)\r\n\r\n"
      + boundary
      + "\r\n"
      + "Content-Description: Delivery report\r\n"
      + "Content-Type: message/delivery-status\r\n\r\n"
      + "X-Postfix-Queue-ID: 12345\r\n"
      + "X-Postfix-Sender: rfc822; test123@example.com\r\n"
      + "Arrival-Date: Wed,  8 Aug 2012 00:05:30 +0900 (JST)\r\n\r\n"
      + boundary
      + "\r\n"
      + "Content-Description: Undelivered Message Headers\r\n"
      + "Content-Type: text/rfc822-headers\r\n"
      + "Content-Transfer-Encoding: 7bit\r\n\r\n"
      + "From: admin@example\r\n"
      + "To: test15@example.com\r\n"
      + "Message-ID: <123456@client.example.com>\r\n"
      + "Subject: test msg";

  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), new SharedByteArrayInputStream(content.getBytes()));

  List<MPartInfo> parts = Mime.getParts(mm);
  assertNotNull(parts);
  assertEquals(4, parts.size());
  MPartInfo mpart = parts.get(0);
  assertEquals("multipart/report", mpart.getContentType());
  List<MPartInfo> children = mpart.getChildren();
  assertEquals(3, children.size());
  Set<String> types =
    Sets.newHashSet("text/plain", "message/delivery-status", "text/rfc822-headers");
  for (MPartInfo child : children) {
   assertTrue(
     types.remove(child.getContentType()), "Expected: " + child.getContentType());
  }

  Set<MPartInfo> bodies = Mime.getBody(parts, false);
  assertEquals(2, bodies.size());

  types = Sets.newHashSet("text/plain", "text/rfc822-headers");
  for (MPartInfo body : bodies) {
   assertTrue(types.remove(body.getContentType()), "Expected: " + body.getContentType());
  }
 }

 @Test
 void emptyMultipart() throws Exception {
  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), getClass().getResourceAsStream("bug50275.txt"));
  List<MPartInfo> parts = Mime.getParts(mm);
  assertNotNull(parts);
  assertEquals(1, parts.size());
  MPartInfo mpart = parts.get(0);
  assertEquals("text/plain", mpart.getContentType());
  assertTrue(
    ((String) mpart.getMimePart().getContent())
      .indexOf("por favor visite http://www.linux-magazine.es/Readers/Newsletter.")
      > -1);
 }

  String baseMpMixedContent =
      "From: user1@example.com\r\n"
          + "To: user2@example.com\r\n"
          + "Subject: test\r\n"
          + "Content-Type: multipart/mixed;\r\n"
          + "boundary=\"----------1111971890AC3BB91\"\r\n";

 @Test
 void multiTextBody() throws Exception {

  StringBuilder content = new StringBuilder(baseMpMixedContent);
  int count = 42;
  for (int i = 0; i < count; i++) {
   content
     .append("------------1111971890AC3BB91\r\n")
     .append("Content-Type: text/html; charset=windows-1250\r\n")
     .append("Content-Transfer-Encoding: quoted-printable\r\n\r\n")
     .append("<html>Body ")
     .append(i)
     .append("</html>\r\n");
  }
  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), new SharedByteArrayInputStream(content.toString().getBytes()));

  List<MPartInfo> parts = Mime.getParts(mm);
  assertNotNull(parts);
  assertEquals(count + 1, parts.size());
  MPartInfo mpart = parts.get(0);
  assertEquals("multipart/mixed", mpart.getContentType());
  List<MPartInfo> children = mpart.getChildren();
  assertEquals(count, children.size());

  Set<MPartInfo> bodies = Mime.getBody(parts, false);
  assertEquals(count, bodies.size());
  for (MPartInfo body : bodies) {
   assertEquals("text/html", body.getContentType());
   Object mimeContent = body.getMimePart().getContent();
   assertTrue(mimeContent instanceof String);
   String string = (String) mimeContent;
   int idx = string.indexOf("" + (body.getPartNum() - 1));
   assertTrue(idx > 0); // body 0 is part 1, b1 is p2, and so on
  }
 }

 @Test
 void imgCid() throws Exception {
  String content =
    baseMpMixedContent
      + "------------1111971890AC3BB91\r\n"
      + "Content-Type: text/html; charset=windows-1250\r\n"
      + "Content-Transfer-Encoding: quoted-printable\r\n\r\n"
      + "<html>Email with img<img src=\"cid:12345_testemail\"/></html>\r\n"
      + "------------1111971890AC3BB91\r\n"
      + "Content-Type: image/jpeg;\r\n"
      + "name=\"img.jpg\"\r\n"
      + "Content-Transfer-Encoding: base64\r\n"
      + "Content-ID: <12345_testemail>\r\n\r\n"
      + "R0a1231312ad124svsdsal=="; // obviously not a real image
  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), new SharedByteArrayInputStream(content.getBytes()));

  List<MPartInfo> parts = Mime.getParts(mm);
  assertNotNull(parts);
  assertEquals(3, parts.size());
  MPartInfo mpart = parts.get(0);
  assertEquals("multipart/mixed", mpart.getContentType());
  List<MPartInfo> children = mpart.getChildren();
  assertEquals(2, children.size());

  Set<MPartInfo> bodies = Mime.getBody(parts, false);
  assertEquals(1, bodies.size());
  MPartInfo body = bodies.iterator().next();
  assertEquals("text/html", body.getContentType());
 }

 @Test
 void textCid() throws Exception {
  String content =
    baseMpMixedContent
      + "------------1111971890AC3BB91\r\n"
      + "Content-Type: text/html; charset=windows-1250\r\n"
      + "Content-Transfer-Encoding: quoted-printable\r\n"
      + "Content-ID: <text_testemail>\r\n\r\n" // barely valid, but we shouldn't break if a
      // bad agent sends like this
      + "<html>Email with img<img src=\"cid:12345_testemail\"/></html>\r\n"
      + "------------1111971890AC3BB91\r\n"
      + "Content-Type: image/jpeg;\r\n"
      + "name=\"img.jpg\"\r\n"
      + "Content-Transfer-Encoding: base64\r\n"
      + "Content-ID: <12345_testemail>\r\n\r\n"
      + "R0a1231312ad124svsdsal=="; // obviously not a real image
  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), new SharedByteArrayInputStream(content.getBytes()));

  List<MPartInfo> parts = Mime.getParts(mm);
  assertNotNull(parts);
  assertEquals(3, parts.size());
  MPartInfo mpart = parts.get(0);
  assertEquals("multipart/mixed", mpart.getContentType());
  List<MPartInfo> children = mpart.getChildren();
  assertEquals(2, children.size());

  Set<MPartInfo> bodies = Mime.getBody(parts, false);
  assertEquals(1, bodies.size());
  MPartInfo body = bodies.iterator().next();
  assertEquals("text/html", body.getContentType());
 }

 @Test
 void imgNoCid() throws Exception {
  String content =
    baseMpMixedContent
      + "------------1111971890AC3BB91\r\n"
      + "Content-Type: text/html; charset=windows-1250\r\n"
      + "Content-Transfer-Encoding: quoted-printable\r\n\r\n"
      + "<html>Email no img</html>\r\n"
      + "------------1111971890AC3BB91\r\n"
      + "Content-Type: image/jpeg;\r\n"
      + "name=\"img.jpg\"\r\n"
      + "Content-Transfer-Encoding: base64\r\n\r\n"
      // no CID here, so sender means for us to show it as body
      + "R0a1231312ad124svsdsal=="; // obviously not a real image
  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), new SharedByteArrayInputStream(content.getBytes()));

  List<MPartInfo> parts = Mime.getParts(mm);
  assertNotNull(parts);
  assertEquals(3, parts.size());
  MPartInfo mpart = parts.get(0);
  assertEquals("multipart/mixed", mpart.getContentType());
  List<MPartInfo> children = mpart.getChildren();
  assertEquals(2, children.size());

  Set<MPartInfo> bodies = Mime.getBody(parts, false);
  assertEquals(2, bodies.size());
  Set<String> types = Sets.newHashSet("text/html", "image/jpeg");
  for (MPartInfo body : bodies) {
   assertTrue(types.remove(body.getContentType()), "Expected: " + body.getContentType());
  }
 }

 @Test
 void fileTypesAsStream() throws Exception {
  fileAsStream("txt", false);
  // not testing all types here, just make sure we don't have special code for pdf somewhere
  fileAsStream("pdf", false);
  fileAsStream("doc", false);
  fileAsStream("xls", false);
 }

 @Test
 void rfc822AsStream() throws Exception {
  fileAsStream("eml", true);
  fileAsStream("msg", true);
 }

  private void fileAsStream(String extension, boolean expectText)
      throws MessagingException, IOException {
    if (extension.charAt(0) == '.') {
      extension = extension.substring(1);
    }
    String content =
        "From: user1@example.com\r\n"
            + "To: user2@example.com\r\n"
            + "Subject: test\r\n"
            + "Content-Type: application/octet-stream;name=\"test."
            + extension
            + "\"\r\n"
            + "Content-Transfer-Encoding: base64\r\n\r\n"
            + "R0a1231312ad124svsdsal=="; // obviously not a real file
    MimeMessage mm =
        new Mime.FixedMimeMessage(
            JMSession.getSession(), new SharedByteArrayInputStream(content.getBytes()));

    MimePart part = Mime.getMimePart(mm, "1");
    String expectedType = expectText ? "text/plain" : "application/octet-stream";
    assertEquals(expectedType, Mime.getContentType(part.getContentType()));
  }

 @Test
 void pdfAsStream() throws Exception {
  String content =
    "From: user1@example.com\r\n"
      + "To: user2@example.com\r\n"
      + "Subject: test\r\n"
      + "Content-Type: application/octet-stream;name=\"test.pdf\"\r\n"
      + "Content-Transfer-Encoding: base64\r\n\r\n"
      + "R0a1231312ad124svsdsal=="; // obviously not a real pdf
  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), new SharedByteArrayInputStream(content.getBytes()));

  MimePart part = Mime.getMimePart(mm, "1");
  assertEquals("application/octet-stream", Mime.getContentType(part.getContentType()));
 }

 @Test
 void semiColonAddressSeparator() throws Exception {
  StringBuilder to = new StringBuilder("To: ");
  int count = 4;
  for (int i = 1; i < count + 1; i++) {
   to.append("user").append(count).append("@example.com").append(";");
  }
  to.setLength(to.length() - 1);
  to.append("\r\n");
  String content =
    "From: user1@example.com\r\n"
      + to.toString()
      + "Subject: test\r\n"
      + "Content-Type: test/plain\r\n\r\n"
      + "test message";

  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), new SharedByteArrayInputStream(content.getBytes()));

  Address[] recipients = mm.getAllRecipients();
  assertEquals(count, recipients.length);
 }

 @Test
 @Disabled("expensive test")
 void bigMime() throws Exception {
  String content = baseMpMixedContent + "\r\n";
  StringBuilder sb = new StringBuilder(content);
  long total = 0;
  int count = 10;
  int partCount = 100;
  int textcount = 1000000;
  int lineSize = 200;
  for (int i = 0; i < partCount; i++) {
   sb.append("------------1111971890AC3BB91\r\n")
     .append("Content-Type: text/plain; charset=windows-1250\r\n")
     .append("Content-Transfer-Encoding: quoted-printable\r\n\r\n");
   for (int j = 0; j < textcount; j += lineSize) {
    String text = RandomStringUtils.randomAlphabetic(lineSize);
    sb.append(text).append("\r\n");
   }
   sb.append("\r\n");
  }
  for (int i = 0; i < count; i++) {
   long start = System.currentTimeMillis();
   MimeMessage mm =
     new Mime.FixedMimeMessage(
       JMSession.getSession(), new SharedByteArrayInputStream(sb.toString().getBytes()));

   List<MPartInfo> parts = Mime.getParts(mm);
   long end = System.currentTimeMillis();
   total += (end - start);
   ZimbraLog.test.info("took %dms", end - start);
   assertNotNull(parts);
   assertEquals(partCount + 1, parts.size());
   MPartInfo body = Mime.getTextBody(parts, false);
   assertNotNull(body);
  }
  ZimbraLog.test.info("Avg %dms", total / count);
 }

 @Test
 void strayBoundaryInEpilogue() throws Exception {
  String content = baseMpMixedContent + "\r\n";
  StringBuilder sb = new StringBuilder(content);
  sb.append("------------1111971890AC3BB91\r\n")
    .append("Content-Type: text/plain; charset=windows-1250\r\n")
    .append("Content-Transfer-Encoding: quoted-printable\r\n\r\n");
  String plainText = RandomStringUtils.randomAlphabetic(10);
  sb.append(plainText).append("\r\n");
  sb.append("------------1111971890AC3BB91--").append("\r\n");

  // this is the point of this test; if MIME has a stray boundary it used to cause NPE
  sb.append("\r\n").append("--bogusBoundary").append("\r\n").append("\r\n");

  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), new SharedByteArrayInputStream(sb.toString().getBytes()));
  List<MPartInfo> parts = Mime.getParts(mm);

  assertEquals(2, parts.size());

  MPartInfo body = Mime.getTextBody(parts, false);
  assertNotNull(body);

  assertTrue(
    TestUtil.bytesEqual(plainText.getBytes(), body.getMimePart().getInputStream()));
 }

 @Test
 void fixBase64LineWrapping() throws Exception {
  String textPlain =
    "Line 1 This is base64 encoded text message part. It does not have line folding. \r\n"
      + "Line 2 This is base64 encoded text message part. It does not have line folding. \r\n"
      + "Line 3 This is base64 encoded text message part. It does not have line folding."
      + " \r\n";
  String textHtml =
    "<html>\r\n"
      + "<body>\r\n"
      + "Line 1 This is base64 encoded html message part. It does not have line folding.  </"
      + " br>Line 2 This is base64 encoded html message part. It does not have line folding."
      + " </ br>\r\n"
      + "Line 3 This is base64 encoded html message part. It does not have line folding. \r\n"
      + "</ br>\r\n"
      + "</body>\r\n"
      + "</html>";
  InputStream is = getClass().getResourceAsStream("base64mime.txt");
  MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession(), is);
  mm.saveChanges();
  String dataBeforeFix = IOUtils.toString(mm.getInputStream());

  Mime.fixBase64MimePartLineFolding(mm);
  mm.saveChanges();
  String dataAfterFix = IOUtils.toString(mm.getInputStream());
  MPartInfo mpiText = Mime.getTextBody(Mime.getParts(mm), false);
  MPartInfo mpiHtml = Mime.getTextBody(Mime.getParts(mm), true);

  assertNotEquals(dataAfterFix, dataBeforeFix, "Line folding should take place.");

  assertEquals(mpiText.getMimePart().getHeader("Content-Transfer-Encoding", ":"), "base64", "Text Part Content-Transfer-Encoding header should be preserved");
  assertEquals(mpiHtml.getMimePart().getHeader("Content-Transfer-Encoding", ":"), "base64", "HTML Part Content-Transfer-Encoding header should be preserved");

  assertEquals(mpiText.mDisposition, "inline", "Text Part Content-Disposition header should be preserved");
  assertEquals(mpiHtml.mDisposition, "inline", "HTML Part Content-Disposition header should be preserved");

  assertTrue(
    TestUtil.bytesEqual(textPlain.getBytes(), mpiText.getMimePart().getInputStream()),
    "Text data should not be modified");
  assertTrue(
    TestUtil.bytesEqual(textHtml.getBytes(), mpiHtml.getMimePart().getInputStream()),
    "Html data should not be modified");
 }

 @Test
 void fixBase64LineWrappingAttachments() throws Exception {
  InputStream is = getClass().getResourceAsStream("bug95114.txt");
  InputStream expectedIs = getClass().getResourceAsStream("bug95114_expected.txt");
  String expected = IOUtils.toString(expectedIs);
  MimeMessage mm = new Mime.FixedMimeMessage(JMSession.getSession(), is);
  mm.saveChanges();
  Mime.fixBase64MimePartLineFolding(mm);
  mm.saveChanges();
  String actual = IOUtils.toString(mm.getInputStream());
  assertEquals(expected, actual, "Content altered.");
 }

 @Test
 void testFullContentType() throws Exception {
  String content =
    "From: user1@example.com\r\n"
      + "To: user2@example.com\r\n"
      + "Subject: test\r\n"
      + "Content-Type: text/plain;param=foo\r\n"
      + "Content-Transfer-Encoding: base64\r\n\r\n"
      + "R0a1231312ad124svsdsal=="; // obviously not a real file
  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), new SharedByteArrayInputStream(content.getBytes()));

  MimePart part = Mime.getMimePart(mm, "1");
  assertEquals("text/plain;param=foo", part.getContentType());
  List<MPartInfo> parts = Mime.getParts(mm);
  assertNotNull(parts);
  assertEquals(1, parts.size());
  MPartInfo info = parts.get(0);
  assertEquals("text/plain", info.getContentType());
  assertEquals("text/plain;param=foo", info.getFullContentType());
 }

 @Test
 void testMultipartPGP() throws Exception {
  String content =
    "From: user1@example.com\r\n"
      + "To: user2@example.com\r\n"
      + "Subject: test\r\n"
      + "Content-Type: multipart/encyrpted;\r\n"
      + " protocol=\"application/pgp-encrypted\";\r\n"
      + " boundary="
      + boundary
      + "\r\n"
      + "Content-Transfer-Encoding: base64\r\n\r\n"
      + "------------1111971890AC3BB91\r\n"
      + "Content-Type: application/pgp-encrypted\r\n"
      + "Content-Description: PGP/MIME version identification\r\n\r\n"
      + "Version: 1\r\n\r\n"
      + "------------1111971890AC3BB91\r\n"
      + "Content-Type: application/octet-stream; name=\"encrypted.asc\"\r\n"
      + "Content-Description: OpenPGP encrypted message\r\n"
      + "Content-Disposition: inline; filename=\"encrypted.asc\"\r\n\r\n"
      + "-----BEGIN PGP MESSAGE-----\r\n"
      + "Version: GnuPG v2.0.22 (GNU/Linux)\r\n\r\n"
      + "o82ejqwkjeh12398123bjkbas731321\r\n" // not a real message, just some placeholder
      // data
      + "-----END PGP MESSAGE-----\r\n\r\n";
  MimeMessage mm =
    new Mime.FixedMimeMessage(
      JMSession.getSession(), new SharedByteArrayInputStream(content.getBytes()));

  List<MPartInfo> parts = Mime.getParts(mm);
  assertNotNull(parts);
  assertEquals(3, parts.size());
  MPartInfo multiPart = parts.get(0);
  assertEquals("multipart/encyrpted", multiPart.getContentType());
  assertEquals(
    "multipart/encyrpted;\r\n protocol=\"application/pgp-encrypted\";\r\n boundary=" + boundary,
    multiPart.getFullContentType());

  MPartInfo pgpVersion = parts.get(1);
  assertEquals("application/pgp-encrypted", pgpVersion.getContentType());

  MPartInfo pgpMsg = parts.get(2);
  assertEquals("application/octet-stream", pgpMsg.getContentType());
  assertEquals(
    "application/octet-stream; name=\"encrypted.asc\"", pgpMsg.getFullContentType());
 }
}

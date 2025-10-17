// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.io.ByteStreams;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.index.IndexDocument;
import com.zimbra.cs.index.LuceneFields;
import com.zimbra.cs.index.analysis.RFC822AddressTokenStream;
import com.zimbra.cs.mime.ParsedMessage.CalendarPartInfo;
import java.util.Arrays;
import java.util.List;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.apache.lucene.document.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ParsedMessage}.
 *
 * @author ysasaki
 */
public final class ParsedMessageTest extends MailboxTestSuite {

 /**
  * @see http://tools.ietf.org/html/rfc2822#appendix-A.5
  */
 @Test
 void rfc2822a5() throws Exception {
  String raw =
    "From: Pete(A wonderful \\) chap) <pete(his account)@(comment)silly.test(his host)>\n" +
      "To: Chris <c@(xxx bbb)public.example>,\n" +
      "         joe@example.org,\n" +
      "  John <jdoe@one.test> (my dear friend); (the end of the group)\n" +
      "Cc:(Empty list)(start)Undisclosed recipients  :(nobody(that I know))  ;\n" +
      "Date: Thu,\n" +
      "      13\n" +
      "        Feb\n" +
      "          1969\n" +
      "      23:32\n" +
      "               -0330 (Newfoundland Time)\n" +
      "Message-ID:              <testabcd.1234@silly.test>\n" +
      "\n" +
      "Testing.";

  ParsedMessage msg = new ParsedMessage(raw.getBytes(), false);
  List<IndexDocument> docs = msg.getLuceneDocuments();
  assertEquals(1, docs.size());
  Document doc = docs.get(0).toDocument();

  RFC822AddressTokenStream from = (RFC822AddressTokenStream) doc.getFieldable(
    LuceneFields.L_H_FROM).tokenStreamValue();
  assertEquals(Arrays.asList("pete", "a", "wonderful", "chap", "pete", "his", "account", "comment",
      "silly.test", "his", "host", "pete@silly.test", "pete", "@silly.test", "silly.test"),
    from.getAllTokens());

  RFC822AddressTokenStream to = (RFC822AddressTokenStream) doc.getFieldable(
    LuceneFields.L_H_TO).tokenStreamValue();
  assertEquals(Arrays.asList("chris", "c@", "c", "xxx", "bbb", "public.example", "joe@example.org", "joe",
    "@example.org", "example.org", "example", "@example", "john", "jdoe@one.test", "jdoe", "@one.test",
    "one.test", "my", "dear", "friend", "the", "end", "of", "the", "group", "c@public.example", "c",
    "@public.example", "public.example"), to.getAllTokens());

  RFC822AddressTokenStream cc = (RFC822AddressTokenStream) doc.getFieldable(
    LuceneFields.L_H_CC).tokenStreamValue();
  assertEquals(Arrays.asList("empty", "list", "start", "undisclosed", "recipients", "nobody", "that", "i",
    "know"), cc.getAllTokens());

  RFC822AddressTokenStream xEnvFrom = (RFC822AddressTokenStream) doc.getFieldable(
    LuceneFields.L_H_X_ENV_FROM).tokenStreamValue();
  assertEquals(0, xEnvFrom.getAllTokens().size());

  RFC822AddressTokenStream xEnvTo = (RFC822AddressTokenStream) doc.getFieldable(
    LuceneFields.L_H_X_ENV_TO).tokenStreamValue();
  assertEquals(0, xEnvTo.getAllTokens().size());
 }

 @Test
 void normalize() {
  testNormalize("normal subject", "foo", "foo", false);
  testNormalize("leading whitespace", " foo", "foo", false);
  testNormalize("trailing whitespace", "foo\t", "foo", false);
  testNormalize("leading and trailing whitespace", "  foo\t", "foo", false);
  testNormalize("compressing whitespace", "foo  bar", "foo bar", false);
  testNormalize("missing subject", null, "", false);
  testNormalize("blank subject", "", "", false);
  testNormalize("nothing but whitespace", "  \t ", "", false);
  testNormalize("mlist prefix", "[bar] foo", "foo", false);
  testNormalize("only a mlist prefix", "[foo]", "[foo]", false);
  testNormalize("broken mlist prefix", "[bar[] foo", "[bar[] foo", false);
  testNormalize("keep only the last mlist prefix", "[bar][baz][foo]", "[foo]", false);
  testNormalize("re: prefix", "re: foo", "foo", true);
  testNormalize("no space after re: prefix", "re:foo", "foo", true);
  testNormalize("re: prefix with leading whitespace", "  re: foo", "foo", true);
  testNormalize("re and [fwd", "re: [fwd: [fwd: re: [fwd: babylon]]]", "babylon", true);
  testNormalize("alternative prefixes", "Ad: Re: Ad: Re: Ad: x", "x", true);
  testNormalize("mlist prefixes, std prefixes, mixed-case fwd trailers",
    "[foo] Fwd: [bar] Re: fw: b (fWd)  (fwd)", "b", true);
  testNormalize("character mixed in with prefixes, mixed-case fwd trailers",
    "[foo] Fwd: [bar] Re: d fw: b (fWd)  (fwd)", "d fw: b", true);
  testNormalize("intermixed prefixes", "Fwd: [Imap-protocol] Re: so long, and thanks for all the fish!",
    "so long, and thanks for all the fish!", true);
 }

    private void testNormalize(String description, String raw, String expected, boolean reply) {
        Pair<String, Boolean> result = ParsedMessage.trimPrefixes(raw);
        String actual = ParsedMessage.compressWhitespace(result.getFirst());
        assertEquals(expected, actual, "[PREFIX] " + description);
        assertEquals(reply, result.getSecond(), "[REPLY] " + description);
        assertEquals(expected, ParsedMessage.normalize(raw), "[NORMALIZE] " + description);
    }


 @Test
 void encryptedFragment() throws Exception {
  String msgWasEncrypted = L10nUtil.getMessage(L10nUtil.MsgKey.encryptedMessageFragment);
  if (msgWasEncrypted == null) {
   ZimbraLog.misc.error("'encryptedMessageFragment' key missing from ZsMsg.properties");
   msgWasEncrypted = "";
  }

  byte[] raw = ByteStreams.toByteArray(getClass().getResourceAsStream("smime-encrypted.txt"));
  ParsedMessage pm = new ParsedMessage(raw, false);
  assertEquals(msgWasEncrypted, pm.getFragment(null), "encrypted-message fragment");

  raw = ByteStreams.toByteArray(getClass().getResourceAsStream("smime-signed.txt"));
  pm = new ParsedMessage(raw, false);
  assertNotEquals(pm.getFragment(null), msgWasEncrypted, "normal message fragment");
 }

 @Test
 void shouldReturnCalendarPartAfterAnalysis_whenMessageHasTextCalendarPartWithoutMethod()
		 throws MessagingException, ServiceException {
  // FIXME: needs mail capabilities to properly parse calendar item.
  //  see: ZMimeBodyPart#38 where we call mc.addMailcap( for example
  final String invite = Util.generateInvite();
  final MimeMessage message = generateMessageWithInvite(invite, "charset=UTF-8");

  final ParsedMessage parsedMessage = new ParsedMessage(message, true);
  parsedMessage.analyzeFully();
  final CalendarPartInfo calendarPartInfo = parsedMessage.getCalendarPartInfo();
  Assertions.assertNotNull(calendarPartInfo, "message has no calendar part");
 }

 private MimeMessage generateMessageWithInvite(String invite, String calendarHeader) throws MessagingException {
  MimeMessage message = new MimeMessage((Session) null);
  message.setFrom(new InternetAddress("organizer@example.com"));
  message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("attendee@example.com"));
  message.setSubject("Meeting Request");

  // Create text part
  MimeBodyPart textPart = new MimeBodyPart();
  textPart.setText("Please see the meeting invitation attached.", "UTF-8");

  // Create calendar part
  MimeBodyPart calendarPart = new MimeBodyPart();
  calendarPart.setDataHandler(
      new DataHandler(
          new ByteArrayDataSource(invite.getBytes(), "text/calendar; charset=UTF-8")
      )
  );

  // Combine into multipart/alternative
  MimeMultipart multipart = new MimeMultipart("alternative");
  multipart.addBodyPart(textPart);
  multipart.addBodyPart(calendarPart);

  // Set content and save
  message.setContent(multipart);
  message.saveChanges();
  return message;
 }
}

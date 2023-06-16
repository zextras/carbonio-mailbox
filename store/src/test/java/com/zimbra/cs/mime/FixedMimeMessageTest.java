// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import javax.mail.internet.MimeMessage;
import javax.mail.util.SharedByteArrayInputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.base.Charsets;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mime.Mime.FixedMimeMessage;
import com.zimbra.cs.util.JMSession;

/**
 * Unit test for {@link FixedMimeMessage}.
 *
 * @author ysasaki
 */
public final class FixedMimeMessageTest {


    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

 @Test
 void messageId() throws Exception {
  String raw = "From: sender@zimbra.com\n" +
    "To: recipient@zimbra.com\n" +
    "Subject: test\n" +
    "\n" +
    "Hello World.";

  MimeMessage message = new FixedMimeMessage(JMSession.getSession(), new SharedByteArrayInputStream(raw.getBytes()));
  assertNull(message.getMessageID());
  message.setHeader("X-TEST", "test");
  message.saveChanges();
  assertNotNull(message.getMessageID());

  raw = "From: sender@zimbra.com\n" +
    "To: recipient@zimbra.com\n" +
    "Subject: test\n" +
    "Message-ID: <12345@zimbra.com>" +
    "\n" +
    "Hello World.";

  message = new FixedMimeMessage(JMSession.getSession(), new SharedByteArrayInputStream(raw.getBytes()));
  assertEquals("<12345@zimbra.com>", message.getMessageID());
  message.setHeader("X-TEST", "test");
  message.saveChanges();
  assertEquals("<12345@zimbra.com>", message.getMessageID());
 }

 @Test
 void contentTransferEncoding() throws Exception {
  String raw = "From: sender@zimbra.com\n" +
    "To: recipient@zimbra.com\n" +
    "Subject: test\n" +
    "Content-Type: text/plain; charset=ISO-2022-JP\n" +
    "\n" +
    "\u3042\u3042\u3042\u3044\u3044\u3044\u3046\u3046\u3046\u3048\u3048\u3048\u304a\u304a\u304a";

  MimeMessage message = new FixedMimeMessage(JMSession.getSession(),
    new SharedByteArrayInputStream(raw.getBytes(Charsets.UTF_8)));
  assertNull(message.getEncoding());
  message.setHeader("X-TEST", "test");
  message.saveChanges();
//        Assert.assertNull(message.getEncoding());

  message = new FixedMimeMessage(JMSession.getSession());
  message.setHeader("X-TEST", "test");
  message.setText("\u3042\u3042\u3042\u3044\u3044\u3044\u3046\u3046\u3046\u3048\u3048\u3048\u304a\u304a\u304a",
    "ISO-2022-JP");
  message.saveChanges();
  assertEquals("7bit", message.getEncoding());

  message = new FixedMimeMessage(JMSession.getSession());
  message.setHeader("X-TEST", "test");
  message.setText("\u3042\u3042\u3042\u3044\u3044\u3044\u3046\u3046\u3046\u3048\u3048\u3048\u304a\u304a\u304a",
    "UTF-8");
  message.saveChanges();
  assertEquals("8bit", message.getEncoding());
 }

}

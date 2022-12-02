// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.zmime;

import java.util.Enumeration;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import junit.framework.Assert;

import org.junit.Test;

public class ZMimeMessageTest {
    private class MimeMessageWithId extends ZMimeMessage {
        private final String mMessageId;

        MimeMessageWithId(String messageId) {
            super(Session.getDefaultInstance(new Properties()));
            mMessageId = messageId;
        }

        @Override
        protected void updateMessageID() throws MessagingException {
            setHeader("Message-ID", mMessageId);
        }
    }

    private static String[] HEADERS = {
        "Date: Mon, 18 Jul 2011 11:30:12 -0700",
        "MIME-Version: 1.0",
        "Subject: re: Your Brains",
        "From: DONOTREPLY@example.com",
        "To: otheruser@example.com",
        "Content-Type: text/plain",
        "X-Face: :/"
    };

    @Test
    public void addHeaderLine() throws Exception {
        MimeMessage mm = new MimeMessageWithId("<sample-823745-asd-23432452345@example.com>");
        for (String line : HEADERS) {
            mm.addHeaderLine(line + "\r\n");
        }
        mm.setContent("", mm.getContentType());
        mm.writeTo(System.out);

        String subject = "re: Your Brains";

        Assert.assertEquals(subject, mm.getSubject());

        @SuppressWarnings("unchecked")
        Enumeration<String> headerLines = mm.getAllHeaderLines();
        boolean foundSubject = false;
        if (headerLines != null) {
            while (headerLines.hasMoreElements()) {
                String line = headerLines.nextElement();
                if (line.startsWith("Subject: ")) {
                    System.out.println(line);
                    Assert.assertEquals("Subject: "+subject, line);
                    foundSubject = true;
                    break;
                }
            }
        }

        Assert.assertTrue(foundSubject);
    }

    @Test
    public void cdisp() throws Exception {
        MimeMessage mm = new MimeMessageWithId("<sample-823745-asd-23432452345@example.com>");
        for (String line : HEADERS) {
            mm.addHeaderLine(line + "\r\n");
        }
        Assert.assertEquals("cdisp unset", null, mm.getDisposition());

        mm.addHeaderLine("Content-Disposition: \r\n");
        Assert.assertEquals("cdisp effectively unset", null, mm.getDisposition());

        mm.setHeader("Content-Disposition", "attachment");
        Assert.assertEquals("cdisp: attachment", "attachment", mm.getDisposition());

        mm.setHeader("Content-Disposition", "inline");
        Assert.assertEquals("cdisp: inline", "inline", mm.getDisposition());

        mm.setHeader("Content-Disposition", "foo");
        Assert.assertEquals("cdisp defaulted", "attachment", mm.getDisposition());
    }

    @Test
    public void iso2022jp() throws Exception {
        MimeMessage mm = new MimeMessageWithId("<sample-823745-asd-23432452345@example.com>");
        String subjectValue =  "\u001b$B@E2,;TKI:R%a!<%k!!EPO?!&JQ99$N$40FFb\u001b(B";
        for (String line : HEADERS) {
            if (line.indexOf("Subject") == 0) {
                mm.addHeaderLine("Subject:" + subjectValue);
            } else {
                mm.addHeaderLine(line + "\r\n");
            }
        }

        @SuppressWarnings("unchecked")
        Enumeration<String> headerLines = mm.getAllHeaderLines();
        boolean foundEncodedSubject = false;
        if (headerLines != null) {
            while (headerLines.hasMoreElements()) {
                String line = headerLines.nextElement();
                if (line.startsWith("Subject: ")) {
                    String encoded = "Subject: =?iso-2022-jp?B?GyRCQEUyLDtUS0k6UiVhITwlayEhRVBPPyEmSlE5OSROJDQwRkZiGyhC?=";
                    //should be equivalent to EncodedWord.encode(subjectValue, Charset.forName("ISO-2022-JP"));
                    Assert.assertEquals(encoded, line);
                    foundEncodedSubject = true;
                    break;
                }
            }
        }
        Assert.assertTrue(foundEncodedSubject);
        //UTF-8 equivalent
        Assert.assertEquals("\u9759\u5ca1\u5e02\u9632\u707d\u30e1\u30fc\u30eb\u3000\u767b\u9332\u30fb\u5909\u66f4\u306e\u3054\u6848\u5185", mm.getSubject());
    }
}

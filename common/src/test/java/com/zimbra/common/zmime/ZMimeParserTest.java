// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.zmime;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.SharedByteArrayInputStream;

import org.junit.jupiter.api.Test;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.CharsetUtil;
import com.zimbra.common.zmime.ZMimeUtility.ByteBuilder;

public class ZMimeParserTest {
    private static String BOUNDARY1 = "-=_sample1";
    private static String BOUNDARY2 = "-=_sample2";

    private ByteBuilder appendMultipartWithoutBoundary(ByteBuilder bb) {
        bb.append("Content-Type: multipart/mixed\r\n");
        bb.append("\r\n");
        bb.append("prologue text goes here\r\n");
        bb.append("--").append(BOUNDARY1).append("\r\n");
        bb.append("Content-Type: text/plain\r\n");
        bb.append("\r\n");
        bb.append("foo!  bar!  loud noises\r\n\r\n");
        bb.append("--").append(BOUNDARY1).append("\r\n");
        bb.append("Content-Type: application/x-unknown\r\n");
        bb.append("Content-Disposition: attachment; filename=x.txt\r\n");
        bb.append("\r\n");
        bb.append("CONTENTS OF ATTACHMENT\r\n\r\n");
        bb.append("--").append(BOUNDARY1).append("--\r\n\r\n");
        return bb;
    }

    private void testMultipartWithoutBoundary(ZMimeMultipart mmp) throws Exception {
        assertEquals("mixed", new ZContentType(mmp.getContentType()).getSubType(), "multipart subtype: mixed");
        assertEquals(2, mmp.getCount(), "multipart has 2 subparts");
        assertEquals(BOUNDARY1, mmp.getBoundary(), "implicit boundary detection");
        assertEquals("text/plain", new ZContentType(mmp.getBodyPart(0).getContentType()).getBaseType(), "first part is text/plain");
        assertEquals("application/x-unknown", new ZContentType(mmp.getBodyPart(1).getContentType()).getBaseType(), "second part is application/x-unknown");
    }

    private Session getSession() {
        return Session.getInstance(new Properties());
    }

  @Test
  void detectBoundary() throws Exception {
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    bb.append("From: <foo@example.com\r\n");
    bb.append("Subject: sample\r\n");
    appendMultipartWithoutBoundary(bb);

    MimeMessage mm = new ZMimeMessage(getSession(), new SharedByteArrayInputStream(bb.toByteArray()));
    assertTrue(mm.getContent() instanceof ZMimeMultipart, "content is multipart");
    testMultipartWithoutBoundary((ZMimeMultipart) mm.getContent());

    bb.reset();
    bb.append("From: <foo@example.com\r\n");
    bb.append("Subject: sample\r\n");
    bb.append("Content-Type: multipart/alternative\r\n");
    bb.append("\r\n");
    bb.append("prologue text goes here\r\n");
    bb.append("--").append(BOUNDARY2).append("\r\n");
    appendMultipartWithoutBoundary(bb);
    bb.append("--").append(BOUNDARY2).append("--\r\n");

    mm = new ZMimeMessage(getSession(), new SharedByteArrayInputStream(bb.toByteArray()));
    assertTrue(mm.getContent() instanceof ZMimeMultipart, "content is multipart");
    ZMimeMultipart mmp = (ZMimeMultipart) mm.getContent();
    assertEquals("alternative", new ZContentType(mmp.getContentType()).getSubType(), "multipart/alternative");
    assertEquals(1, mmp.getCount(), "toplevel multipart has 1 subpart");
    assertEquals(BOUNDARY2, mmp.getBoundary(), "implicit boundary detection");
    assertEquals("multipart/mixed", new ZContentType(mmp.getBodyPart(0).getContentType()).getBaseType(), "first part is multipart/mixed");
    testMultipartWithoutBoundary((ZMimeMultipart) mmp.getBodyPart(0).getContent());
  }

  @Test
  void multipleContentTypes() throws Exception {
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    bb.append("Content-Type: text/plain\r\n");
    bb.append("From: <foo@example.com\r\n");
    bb.append("Subject: sample\r\n");
    bb.append("Content-Type: multipart/alternative; boundary=").append(BOUNDARY1).append("\r\n");
    bb.append("\r\n");
    bb.append("--").append(BOUNDARY1).append("\r\n");
    bb.append("Content-Type: text/plain\r\n");
    bb.append("\r\n");
    bb.append("foo!  bar!  loud noises\r\n\r\n");
    bb.append("--").append(BOUNDARY1).append("--\r\n");

    try {
      MimeMessage mm = new ZMimeMessage(getSession(), new SharedByteArrayInputStream(bb.toByteArray()));
      assertFalse(mm.getContent() instanceof MimeMultipart, "content isn't multipart");
      assertEquals("text/plain", new ZContentType(mm.getContentType()).getBaseType(), "text/plain");
    } catch (ClassCastException e) {
      fail("mishandled double Content-Type headers");
    }
  }

  @Test
  void parse() throws Exception {
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    bb.append("Content-Type: text/plain\r\n");
    bb.append("From: <foo@example.com\r\n");
    bb.append("Subject: sample\r\n");
    bb.append("Content-Type: multipart/alternative; boundary=").append(BOUNDARY1).append("\r\n");
    bb.append("\r\n");
    bb.append("--").append(BOUNDARY1).append("\r\n");
    bb.append("Content-Type: text/plain\r\n");
    bb.append("\r\n");
    bb.append("foo!  bar!  loud noises\r\n\r\n");
    bb.append("--").append(BOUNDARY1).append("--\r\n");

    try {
      MimeMessage mm = ZMimeParser.parse(getSession(), new SharedByteArrayInputStream(bb.toByteArray()));
      assertFalse(mm.getContent() instanceof MimeMultipart, "content isn't multipart");
      assertEquals("text/plain", new ZContentType(mm.getContentType()).getBaseType(), "text/plain");
    } catch (ClassCastException e) {
      fail("mishandled double Content-Type headers");
    }
  }

  @Test
  void repetition() throws Exception {
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    String boundary = BOUNDARY1;
    bb.append("From: <foo@example.com\r\n");
    bb.append("Subject: sample\r\n");
    bb.append("Content-Type: multipart/mixed; boundary=").append(boundary).append("\r\n");
    for (int i = 0;i < 100;i++) {
      bb.append("--").append(boundary).append("\r\n");
      bb.append("Content-Type: text/plain\r\n");
      bb.append("\r\n");
      bb.append("foo!  bar!  loud noises\r\n\r\n");
    }
    bb.append("--").append(boundary).append("--\r\n");
    try {
      MimeMessage mm = ZMimeParser.parse(getSession(), new SharedByteArrayInputStream(bb.toByteArray()));
      Object content = mm.getContent();
      assertTrue(content instanceof MimeMultipart, "content is multipart");
      MimeMultipart mp = (MimeMultipart) content;
      assertEquals(100, mp.getCount(), "count reduced??");
    } catch (ClassCastException e) {
      fail("mishandled double Content-Type headers");
    }
  }

    private void addChildren(ByteBuilder bb, int depth) {
        //recursively add children to create deep MIME tree
        if (depth == 100) {
            return;
        }
        String boundary = "-=_level" + depth;
        bb.append("Content-Type: multipart/mixed; boundary=").append(boundary).append("\r\n");
        bb.append("--").append(boundary).append("\r\n");
        bb.append("Content-Type: text/plain\r\n");
        bb.append("\r\n");
        bb.append("foo!  bar!  loud noises\r\n\r\n");
        bb.append("--").append(boundary).append("\r\n");
        addChildren(bb, depth+1);
        bb.append("--").append(boundary).append("--\r\n");
    }

    private void traverseChildren(ZMimeMultipart mp, int targetDepth) throws MessagingException, IOException {
        //traverse MIME tree and make sure the expected bottom item has no children
        if (targetDepth == 0) {
          assertEquals(mp.getCount(), 0, "depth at 0");
            return;
        }
        BodyPart bp = mp.getBodyPart(1);
        assertTrue(bp instanceof ZMimeBodyPart, "not multipart?");
        ZMimeBodyPart zbp = (ZMimeBodyPart) bp;
        Object content = zbp.getContent();
        assertTrue(content instanceof ZMimeMultipart, "not multipart?");
        ZMimeMultipart zmp = (ZMimeMultipart) content;
        traverseChildren(zmp, targetDepth - 1);
    }

  @Test
  void recursion() throws Exception {
    ByteBuilder bb = new ByteBuilder(CharsetUtil.UTF_8);
    String boundary = BOUNDARY1;
    bb.append("From: <foo@example.com\r\n");
    bb.append("Subject: sample\r\n");
    bb.append("Content-Type: multipart/mixed; boundary=").append(boundary).append("\r\n");
    bb.append("Content-Type: text/plain\r\n");
    bb.append("\r\n");
    bb.append("foo!  bar!  loud noises\r\n\r\n");
    bb.append("--").append(boundary).append("\r\n");
    addChildren(bb, 0);
    bb.append("--").append(boundary).append("--\r\n");
    try {
      MimeMessage mm = ZMimeParser.parse(getSession(), new SharedByteArrayInputStream(bb.toByteArray()));
      Object content = mm.getContent();
      assertTrue(content instanceof ZMimeMultipart, "content is multipart");
      ZMimeMultipart zmp = (ZMimeMultipart) content;
      assertEquals(1, zmp.getCount(), "top count");
      traverseChildren((ZMimeMultipart) zmp.getBodyPart(0).getContent(), LC.mime_max_recursion.intValue() - 1);
    } catch (ClassCastException e) {
      fail("mishandled double Content-Type headers");
    }
  }

    //    private static void checkFile(java.io.File file) throws Exception {
//        String name = file.getName();
//        Properties props = new Properties();
//
//        props.put("mail.mime.address.strict", "false");
//
//        String charset = null;
//        if (name.startsWith("gbk") || name.startsWith("gb2312")) {
//            charset = "gb2312";
//        } else if (name.startsWith("iso-8859-1")) {
//            charset = "iso-8859-1";
//        } else if (name.startsWith("iso-8859-2")) {
//            charset = "iso-8859-2";
//        } else if (name.startsWith("iso-2022-jp")) {
//            charset = "iso-2022-jp";
//        } else if (name.startsWith("shift_jis")) {
//            charset = "shift_jis";
//        } else if (name.startsWith("big5")) {
//            charset = "big5";
//        }
//        if (charset != null) {
//            props.put("mail.mime.charset", charset);
//            props.put(com.zimbra.common.mime.MimePart.PROP_CHARSET_DEFAULT, charset);
//        }
//
//        Session s = Session.getInstance(props);
//        MimeMessage zmm = new ZMimeMessage(s, new java.io.FileInputStream(file));
//        MimeMessage jmmm = new com.zimbra.common.mime.shim.JavaMailMimeMessage(s, new javax.mail.util.SharedFileInputStream(file));
//        MimeMessage mm = new MimeMessage(s, new java.io.FileInputStream(file));
//
//        System.out.println("checking file: " + file.getName() + " [zmm/mm]");
//        com.zimbra.common.mime.shim.JavaMailMimeTester.compareStructure(zmm, mm);
//        System.out.println("checking file: " + file.getName() + " [jmmm/zmm]");
//        com.zimbra.common.mime.shim.JavaMailMimeTester.compareStructure(jmmm, zmm);
//    }
//
//    @Test
//    public void simple() throws Exception {
//        System.setProperty("mail.mime.decodetext.strict",   "false");
//        System.setProperty("mail.mime.encodefilename",      "true");
//        System.setProperty("mail.mime.charset",             "utf-8");
//        System.setProperty("mail.mime.base64.ignoreerrors", "true");
//
//        checkFile(new java.io.File("/Users/dkarp/Documents/messages/undisplayed-generated"));
//
//        for (java.io.File file : new java.io.File("/Users/dkarp/Documents/messages").listFiles()) {
//            if (file.isFile()) {
//                checkFile(file);
//            }
//        }
//    }
}

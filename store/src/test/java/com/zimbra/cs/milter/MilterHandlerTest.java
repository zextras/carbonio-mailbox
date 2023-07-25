// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.milter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.junit.jupiter.api.Test;

import com.zimbra.common.mime.InternetAddress;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.mime.MimeAddressHeader;


/**
 * @author zimbra
 *
 */
public class MilterHandlerTest{
 @Test
 void testGetToCcAddressHeaderNonAscii() throws IOException {
  String to = "To" + '\0' + "R\u00e9\u4f60\u597d < toadmin@example.com>";
  String cc = "CC" + '\0' + "\ud835\udd18\ud835\udd2b\ud835\udd26\ud835\udd20\ud835\udd2c\ud835\udd21\u4f60\u597d <ccadmin@example.com>";
  String from = "from" + '\0' + "\u00e9\u0326\u4e16\u754c <admin@example.com>";
  MimeAddressHeader mh = MilterHandler.getToCcAddressHeader(to.getBytes("utf-8"));
  assertNotNull(mh);
  assertEquals("toadmin@example.com", mh.getAddresses().get(0).getAddress());
  mh = MilterHandler.getToCcAddressHeader(cc.getBytes("utf-8"));
  assertNotNull(mh);
  assertEquals(1, mh.getAddresses().size());
  assertEquals("ccadmin@example.com", mh.getAddresses().get(0).getAddress());
  mh = MilterHandler.getToCcAddressHeader(from.getBytes("utf-8"));
  assertNull(mh);
 }

 @Test
 void testGetToCcAddressHeaderNonAscii1() throws IOException {
  List<String> expctedEmails = new ArrayList<String>();
  expctedEmails.add("toadmin@example.com");
  expctedEmails.add("test@example.com");
  expctedEmails.add("test2@example.com");
  String to = "To" + '\0' + "R\u00e9\u4f60\u597d <toadmin@example.com>,test@example.com,<test2@example.com>";
  MimeAddressHeader mh = MilterHandler.getToCcAddressHeader(to.getBytes("iso-8859-1"));
  assertEquals(3, mh.getAddresses().size());
  for (InternetAddress addrs : mh.getAddresses()) {
   expctedEmails.contains(addrs.getAddress());
  }
 }

 @Test
 void testGetToCcAddressHeaderAscii() throws IOException {
  String to = "To" + '\0' + "admin@example.com; admin2@example.com";
  List<String> expctedEmails = new ArrayList<String>();
  expctedEmails.add("admin@example.com");
  expctedEmails.add("admin2@example.com");
  MimeAddressHeader mh = MilterHandler.getToCcAddressHeader(to.getBytes("iso-8859-1"));
  assertEquals(2, mh.getAddresses().size());
  for (InternetAddress addrs : mh.getAddresses()) {
   expctedEmails.contains(addrs.getAddress());
  }
  to = "To" + '\0';
  mh = MilterHandler.getToCcAddressHeader(to.getBytes("iso-8859-1"));
  assertNull(mh);
 }

 @Test
 void testParseMicro() throws IOException {
  String to = "To" + '\0' + "admin@example.com; admin2@example.com";
  byte [] b = to.getBytes("iso-8859-1");
  IoBuffer buf = getIoBuffer(b);
  Map<String, String> address = MilterHandler.parseMacros(buf);
  assertEquals(1, address.size());
 }

 @Test
 void testParseMicroNonAscii() throws IOException {
  String to = "To" + '\0' + "R\u00e9\u4f60\u597d <toadmin@example.com>";
  byte [] b = to.getBytes("iso-8859-1");
  IoBuffer buf = getIoBuffer(b);
  Map<String, String> address = MilterHandler.parseMacros(buf);
  assertEquals(0, address.size());
 }

    static IoBuffer getIoBuffer(byte [] b) {
        IoBuffer buf = IoBuffer.allocate(b.length, false);
        buf.put(b);
        buf.flip();
        return buf;
    }

}

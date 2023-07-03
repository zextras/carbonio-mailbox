// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.base.Charsets;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mime.MPartInfo;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.FeedManager.RemoteDataInfo;
import com.zimbra.cs.service.FeedManager.SubscriptionData;

public class FeedManagerTest {

    @BeforeEach
    public void setUp() {
        LC.zimbra_feed_manager_blacklist.setDefault("10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,fd00::/8");
        LC.zimbra_feed_manager_whitelist.setDefault("");
    }

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

 @Test
 void subject() throws Exception {
  assertEquals("", FeedManager.stripXML(null), "null");
  assertEquals("test subject", FeedManager.stripXML("test subject"), "no transform");
  assertEquals("test subject test", FeedManager.stripXML("test <a>subject</a> test"), "link");
  assertEquals("test subject", FeedManager.stripXML("test su<a>bject</a>"), "embed link");
  assertEquals("test subject test", FeedManager.stripXML("test <b>subject</b> test"), "bold");
  assertEquals("test subject", FeedManager.stripXML("test<br>subject"), "break");
  assertEquals("test subject", FeedManager.stripXML("test <br>subject"), "space break");
 }

 @Test
 @Disabled("add missing xml file")
 void socialcastAtomFeed() throws Exception {
  long lastModified = 0;
  String expectedCharset = MimeConstants.P_CHARSET_UTF8;
  BufferedInputStream content = new BufferedInputStream(getClass().getResourceAsStream("socialcastAtomFeed.xml"));
  RemoteDataInfo rdi = new RemoteDataInfo(HttpStatus.OK_200, 0, content, expectedCharset, lastModified);
  SubscriptionData<?> subsData = FeedManager.retrieveRemoteDatasource(null, rdi, null);
  List<?> subs = subsData.getItems();
  assertNotNull(subs, "List of subscriptions");
  assertEquals(1, subs.size(), "Number of items");
  for (Object obj : subs) {
   if (obj instanceof ParsedMessage) {
    ParsedMessage pm = (ParsedMessage) obj;
    List<MPartInfo> parts = pm.getMessageParts();
    assertEquals(1, parts.size(), "Number of message parts");
    String msgContent = streamToString(parts.get(0).getMimePart().getInputStream(), Charsets.UTF_8);
    assertTrue(msgContent.indexOf("Congratulations for passing!") > 0, "Text from inside <div>");
    assertTrue(msgContent.indexOf(
      "https://pink.socialcast.com/messages/15629747-active-learner-thanks-to-cccc") > 0,
      "Article reference");
   } else {
    fail("Expecting a ParsedMessage where is " + obj.getClass().getName());
   }
  }
 }

 @Test
 @Disabled("add missing xml file")
 void atomEnabledOrg() throws Exception {
  long lastModified = 0;
  String expectedCharset = MimeConstants.P_CHARSET_UTF8;
  BufferedInputStream content = new BufferedInputStream(getClass().getResourceAsStream("atomEnabledOrg.xml"));
  RemoteDataInfo rdi = new RemoteDataInfo(HttpStatus.OK_200, 0, content, expectedCharset, lastModified);
  SubscriptionData<?> subsData = FeedManager.retrieveRemoteDatasource(null, rdi, null);
  List<?> subs = subsData.getItems();
  assertNotNull(subs, "List of subscriptions");
  assertEquals(2, subs.size(), "Number of items");
  Object obj;
  obj = subs.get(0);
  if (obj instanceof ParsedMessage) {
   ParsedMessage pm = (ParsedMessage) obj;
   List<MPartInfo> parts = pm.getMessageParts();
   assertEquals(1, parts.size(), "Number of message parts");
   String msgContent = streamToString(parts.get(0).getMimePart().getInputStream(), Charsets.UTF_8);
   assertTrue(msgContent.indexOf("Rev 0.9 of the AtomAPI has just been posted") > 0,
     "Some content text");
  } else {
   fail("Expecting a ParsedMessage where is " + obj.getClass().getName());
  }
  obj = subs.get(1);
  if (obj instanceof ParsedMessage) {
   ParsedMessage pm = (ParsedMessage) obj;
   List<MPartInfo> parts = pm.getMessageParts();
   assertEquals(1, parts.size(), "Number of message parts");
   String msgContent = streamToString(parts.get(0).getMimePart().getInputStream(), Charsets.UTF_8);
   assertTrue(msgContent.indexOf("AtomAPI at ApacheCon in Las Vegas") > 0, "Some content text");
  } else {
   fail("Expecting a ParsedMessage where is " + obj.getClass().getName());
  }
 }

 @Test
 void testIsInRangePrivateAddressesIPv4() throws Exception {
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("9.0.0.0"), "10.0.0.0/8"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("9.255.255.255"), "10.0.0.0/8"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("10.0.0.0"), "10.0.0.0/8"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("10.50.50.55"), "10.0.0.0/8"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("10.50.0.255"), "10.0.0.0/8"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("10.50.255.0"), "10.0.0.0/8"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("10.255.255.255"), "10.0.0.0/8"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("11.0.0.0"), "10.0.0.0/8"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("11.255.255.255"), "10.0.0.0/8"));

  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("172.15.255.255"), "172.16.0.0/12"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("172.15.0.0"), "172.16.0.0/12"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("172.16.0.0"), "172.16.0.0/12"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("172.16.50.50"), "172.16.0.0/12"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("172.16.0.255"), "172.16.0.0/12"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("172.16.255.0"), "172.16.0.0/12"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("172.31.255.255"), "172.16.0.0/12"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("172.32.0.0"), "172.16.0.0/12"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("172.32.255.255"), "172.16.0.0/12"));

  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("192.167.0.0"), "192.168.0.0/16"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("192.167.255.255"), "192.168.0.0/16"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("192.168.0.0"), "192.168.0.0/16"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("192.168.1.131"), "192.168.0.0/16"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("192.168.0.255"), "192.168.0.0/16"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("192.168.255.0"), "192.168.0.0/16"));
  assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("192.168.255.255"), "192.168.0.0/16"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("192.169.255.255"), "192.168.0.0/16"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("192.169.0.0"), "192.168.0.0/16"));
 }

 @Test
 void testIsInRangeTestAddressesIPv4() throws Exception {
  for (int i = 0; i < 256; i++) {
   assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("198.51.100." + i), "198.51.100.0/24"));
  }
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("198.50.100.0"), "198.51.100.0/24"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("198.50.100.255"), "198.51.100.0/24"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("198.52.100.0"), "198.51.100.0/24"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("198.52.100.255"), "198.51.100.0/24"));

  for (int i = 0; i < 256; i++) {
   assertTrue(FeedManager.isAddressInRange(InetAddress.getByName("203.0.113." + i), "203.0.113.0/24"));
  }
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("203.0.112.0"), "203.0.113.0/24"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("203.0.112.255"), "203.0.113.0/24"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("203.0.114.0"), "203.0.113.0/24"));
  assertFalse(FeedManager.isAddressInRange(InetAddress.getByName("203.0.114.255"), "203.0.113.0/24"));
 }

 @Test
 void testIsInRangePrivateAddressesIPv6() throws Exception {
  assertFalse(FeedManager.isAddressInRange(
    InetAddress.getByName("fedd:0d17:76f7:3e82:0000:0000:0000:0000"), "fddd:0d17:76f7:3e82::/64"));
  assertFalse(FeedManager.isAddressInRange(
    InetAddress.getByName("fedd:0d17:76f7:3e82:ffff:ffff:ffff:ffff"), "fddd:0d17:76f7:3e82::/64"));
  assertTrue(FeedManager.isAddressInRange(
    InetAddress.getByName("fddd:0d17:76f7:3e82:0000:0000:0000:0000"), "fddd:0d17:76f7:3e82::/64"));
  assertTrue(FeedManager.isAddressInRange(
    InetAddress.getByName("fddd:0d17:76f7:3e82:1111:1234:5678:abcd"), "fddd:0d17:76f7:3e82::/64"));
  assertTrue(FeedManager.isAddressInRange(
    InetAddress.getByName("fddd:0d17:76f7:3e82:ffff:ffff:ffff:ffff"), "fddd:0d17:76f7:3e82::/64"));
  assertFalse(FeedManager.isAddressInRange(
    InetAddress.getByName("fddd:0d17:76f7:3e83:0000:0000:0000:0000"), "fddd:0d17:76f7:3e82::/64"));
  assertFalse(FeedManager.isAddressInRange(
    InetAddress.getByName("fddd:0d17:76f7:3e83:ffff:ffff:ffff:ffff"), "fddd:0d17:76f7:3e82::/64"));

  assertFalse(FeedManager.isAddressInRange(
    InetAddress.getByName("fcdd:0d17:76f7:3e82:0000:0000:0000:0000"), "fd00::/8"));
  assertFalse(FeedManager.isAddressInRange(
    InetAddress.getByName("fcdd:0d17:76f7:3e82:ffff:ffff:ffff:ffff"), "fd00::/8"));
  assertTrue(FeedManager.isAddressInRange(
    InetAddress.getByName("fddd:0d17:76f7:3e82:0000:0000:0000:0000"), "fd00::/8"));
  assertTrue(FeedManager.isAddressInRange(
    InetAddress.getByName("fddd:0d17:76f7:3e82:1111:755f:ffff:0d17"), "fd00::/8"));
  assertTrue(FeedManager.isAddressInRange(
    InetAddress.getByName("fddd:0d17:76f7:3e82:ffff:ffff:ffff:ffff"), "fd00::/8"));
  assertFalse(FeedManager.isAddressInRange(
    InetAddress.getByName("fedd:0d17:76f7:3e82:0000:0000:0000:0000"), "fd00::/8"));
  assertFalse(FeedManager.isAddressInRange(
    InetAddress.getByName("fedd:0d17:76f7:3e82:ffff:ffff:ffff:ffff"), "fd00::/8"));
 }

 @Test
 void testIsInRangeSingleAddressIPv4() throws Exception {
  assertTrue(FeedManager.isAddressInRange(
    InetAddress.getByName("192.168.1.0"), "192.168.1.0"));
  for (int i = 1; i < 256; i++) {
   assertTrue(FeedManager.isAddressInRange(
     InetAddress.getByName("192.168.1." + i), "192.168.1." + i));
   assertFalse(FeedManager.isAddressInRange(
     InetAddress.getByName("192.168.1." + i), "192.168.1.0"));
  }
 }

 @Test
 void testIsInRangeSingleAddressIPv6() throws Exception {
  assertTrue(FeedManager.isAddressInRange(
    InetAddress.getByName("fddd:0d17:76f7:3e82:0000:0000:ffff:ffff"),
    "fddd:0d17:76f7:3e82:0000:0000:ffff:ffff"));
  assertTrue(FeedManager.isAddressInRange(
    InetAddress.getByName("fddd:0d17:76f7:3e82:1234:5678:abcd:efff"),
    "fddd:0d17:76f7:3e82:1234:5678:abcd:efff"));
  assertFalse(FeedManager.isAddressInRange(
    InetAddress.getByName("fddd:0d17:0000:3e82:0000:0000:ffff:ffff"),
    "fddd:0d17:76f7:3e82:0000:0000:ffff:0000"));
 }

 @Test
 void testIsBlockedFeedAddressDefaultBlacklist() throws Exception {
  // loopback
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://localhost/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://localhost:8085/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://127.0.0.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://127.0.0.1:8085/feed")));

  // private
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://172.16.150.140/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://172.25.150.140/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://user:pass@192.168.5.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.5.1:8080/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.166.150/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.166.150:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://10.0.0.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://10.15.150.140/feed")));
 }

 @Test
 void testIsBlockedFeedAddressDefaultBlacklistWithWhitelistedIp() throws Exception {
  LC.zimbra_feed_manager_whitelist.setDefault("192.168.1.106");

  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.1.106/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.1.106:8080/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://user:pass@192.168.1.106/feed")));

  // loopback
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://localhost/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://localhost:8085/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://127.0.0.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://127.0.0.1:8085/feed")));

  // private
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://172.16.150.140/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://172.25.150.140/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://user:pass@192.168.5.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.5.1:8080/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.166.150/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.166.150:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://10.0.0.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://10.15.150.140/feed")));
 }

 @Test
 void testIsBlockedFeedAddressDefaultBlacklistWithWhitelistedRange() throws Exception {
  LC.zimbra_feed_manager_whitelist.setDefault("192.168.100.0/25");

  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.100.0/feed")));
  for (int i = 1; i < 128; i++) {
   assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.100." + i + "/feed")));
  }
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.100.128/feed")));

  // loopback
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://localhost/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://localhost:8085/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://127.0.0.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://127.0.0.1:8085/feed")));

  // private
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://172.16.150.140/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://172.25.150.140/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://user:pass@192.168.5.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.5.1:8080/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.166.150/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.166.150:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://10.0.0.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://10.15.150.140/feed")));
 }

 @Test
 void testIsBlockedFeedAddressDefaultBlacklistWithWhitelistedMultiple() throws Exception {
  LC.zimbra_feed_manager_whitelist.setDefault("192.168.100.0/25,192.168.105.122,10.12.150.101");

  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.105.122/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://10.12.150.101/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.100.0/feed")));
  for (int i = 1; i < 128; i++) {
   assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.100." + i + "/feed")));
  }
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.100.128/feed")));

  // loopback
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://localhost/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://localhost:8085/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://127.0.0.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://127.0.0.1:8085/feed")));

  // private
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://172.16.150.140/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://172.25.150.140/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://user:pass@192.168.5.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.5.1:8080/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.166.150/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.166.150:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://10.0.0.1/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://10.15.150.140/feed")));
 }

 @Test
 void testIsBlockedFeedAddressPublicBlacklisted() throws Exception {
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.230:8081/feed")));
  LC.zimbra_feed_manager_blacklist.setDefault("198.51.100.230");
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.230:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("https://198.51.100.230:8082/feed")));
 }

 @Test
 void testIsBlockedFeedAddressPublicBlacklistedMultiple() throws Exception {
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.231:8081/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.220:8081/feed")));
  LC.zimbra_feed_manager_blacklist.setDefault("198.51.100.230,198.51.100.231,198.51.100.220");
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.230:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("https://198.51.100.230:8082/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.220:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.231:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://user:pass@198.51.100.231:8081/feed")));
 }

 @Test
 void testIsBlockedFeedAddressPublicBlacklistCIDR() throws Exception {
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.130:8081/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.120:8081/feed")));
  LC.zimbra_feed_manager_blacklist.setDefault("198.51.100.0/25");
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.95:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("https://198.51.100.95:8082/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.101:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.127:8081/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.128:8081/feed")));
 }

 @Test
 void testIsBlockedFeedAddressPublicBlacklistCIDRMultiple() throws Exception {
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://203.0.113.167:8081/feed")));
  LC.zimbra_feed_manager_blacklist.setDefault("198.51.100.0/24,203.0.113.0/24,192.0.2.121");
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://203.0.113.227:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://203.0.113.167:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://198.51.100.120:8081/feed")));
  assertTrue(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.0.2.121:8081/feed")));
 }

 @Test
 void testIsBlockedFeedAddressPublicTest() throws Exception {
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://zimbra.test:8080/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("https://zimbra.test/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("https://user:pass@zimbra.test/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("https://user:pass@192.0.2.165/feed")));
 }

 @Test
 void testIsBlockedFeedAddressPrivateNoBlacklist() throws Exception {
  LC.zimbra_feed_manager_blacklist.setDefault("");
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://10.15.150.140/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://172.16.150.140/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://192.168.5.1/feed")));
 }

 @Test
 void testIsBlockedFeedAddressUnknownAddressNoBlacklist() throws Exception {
  LC.zimbra_feed_manager_blacklist.setDefault("");
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://fake.test/feed")));
  assertFalse(FeedManager.isBlockedFeedAddress(new URIBuilder("http://example.test/feed")));
 }

    public static String streamToString(InputStream stream, Charset cs)
    throws IOException {
        try {
            Reader reader = new BufferedReader(
                    new InputStreamReader(stream, cs));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } finally {
            stream.close();
        }
    }
}

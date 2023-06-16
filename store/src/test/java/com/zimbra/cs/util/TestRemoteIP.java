// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.zimbra.common.util.RemoteIP;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.service.MockHttpServletRequest;

public class TestRemoteIP {

    @BeforeEach
    public void setUp() {
        ZimbraLog.clearContext();
    }

    @AfterEach
    public void tearDown() {
        ZimbraLog.clearContext();
    }

 @Test
 void testXForwardedHeaders() throws UnsupportedEncodingException, MalformedURLException {
  HashMap<String, String> headers = new HashMap<String, String>();
  headers.put(RemoteIP.X_ORIGINATING_IP_HEADER, "172.16.150.11");
  headers.put(RemoteIP.X_ORIGINATING_PORT_HEADER, "8080");
  headers.put(RemoteIP.X_ORIGINATING_PROTOCOL_HEADER, "IMAP");
  MockHttpServletRequest req = new MockHttpServletRequest("test".getBytes("UTF-8"), new URL(
    "http://localhost:7070/service/FooRequest"), "", 80, "192.168.1.1", headers);
  RemoteIP remoteIp = new RemoteIP(req, new RemoteIP.TrustedIPs(new String[]{"192.168.1.1"}));
  assertEquals("172.16.150.11", remoteIp.getOrigIP(), "wrong originating IP");
  assertEquals("8080", remoteIp.getOrigPort().toString(), "wrong originating port");
  assertEquals("IMAP", remoteIp.getOrigProto(), "wrong originating protocol");
  assertEquals("172.16.150.11", remoteIp.getRequestIP(), "wrong request IP");
  assertEquals("8080", remoteIp.getRequestPort().toString(), "wrong request port");
  assertEquals("192.168.1.1", remoteIp.getClientIP(), "wrong client IP");
  assertEquals("80", remoteIp.getClientPort().toString(), "wrong client port");
 }

 @Test
 void testNoXForwardedHeaders() throws UnsupportedEncodingException, MalformedURLException {
  HashMap<String, String> headers = new HashMap<String, String>();
  MockHttpServletRequest req = new MockHttpServletRequest("test".getBytes("UTF-8"), new URL(
    "http://localhost:7070/service/FooRequest"), "", 80, "192.168.1.1", headers);
  RemoteIP remoteIp = new RemoteIP(req, new RemoteIP.TrustedIPs(new String[]{"192.168.1.1"}));
  assertNull(remoteIp.getOrigIP(), "originating IP should be null");
  assertNull(remoteIp.getOrigPort(), "originating port should be null");
  assertNull(remoteIp.getOrigProto(), "originating protocol should be null");
  assertEquals("192.168.1.1", remoteIp.getRequestIP(), "wrong request IP");
  assertEquals("80", remoteIp.getRequestPort().toString(), "wrong request port");
  assertEquals("192.168.1.1", remoteIp.getClientIP(), "wrong client IP");
  assertEquals("80", remoteIp.getClientPort().toString(), "wrong client port");
 }

 @Test
 void testNonTrustedClientIPRemoteIP() throws UnsupportedEncodingException, MalformedURLException {
  HashMap<String, String> headers = new HashMap<String, String>();
  headers.put(RemoteIP.X_ORIGINATING_PROTOCOL_HEADER, "IMAP");
  MockHttpServletRequest req = new MockHttpServletRequest("test".getBytes("UTF-8"), new URL(
    "http://localhost:7070/service/FooRequest"), "", 80, "10.10.1.1", headers);
  RemoteIP remoteIp = new RemoteIP(req, new RemoteIP.TrustedIPs(new String[]{"192.168.1.1"}));
  // we should ignore X-Forwarded-XXX headers from non-trusted clients
  assertNull(remoteIp.getOrigIP(), "originating IP should be null");
  assertNull(remoteIp.getOrigPort(), "originating port should be null");
  assertNull(remoteIp.getOrigProto(), "originating protocol should be null");
  assertEquals("10.10.1.1", remoteIp.getRequestIP(), "wrong request IP");
  assertEquals("80", remoteIp.getRequestPort().toString(), "wrong request port");
  assertEquals("10.10.1.1", remoteIp.getClientIP(), "wrong client IP");
  assertEquals("80", remoteIp.getClientPort().toString(), "wrong client port");
 }

 @Test
 void testTrustedIPLogString() throws Exception {
  HashMap<String, String> headers = new HashMap<String, String>();
  headers.put(RemoteIP.X_ORIGINATING_IP_HEADER, "172.16.150.11");
  headers.put(RemoteIP.X_ORIGINATING_PORT_HEADER, "8080");
  headers.put(RemoteIP.X_ORIGINATING_PROTOCOL_HEADER, "IMAP");
  MockHttpServletRequest req = new MockHttpServletRequest("test".getBytes("UTF-8"), new URL(
    "http://localhost:7070/service/FooRequest"), "", 80, "192.168.1.1", headers);
  RemoteIP remoteIp = new RemoteIP(req, new RemoteIP.TrustedIPs(new String[]{"192.168.1.1"}));
  remoteIp.addToLoggingContext();
  String updatedLogContext = ZimbraLog.getContextString();
  assertTrue(updatedLogContext.indexOf("oip=172.16.150.11") > -1);
  assertTrue(updatedLogContext.indexOf("oport=8080") > -1);
  assertTrue(updatedLogContext.indexOf("ip=172.16.150.11") > -1);
  assertTrue(updatedLogContext.indexOf("port=8080") > -1);
  assertTrue(updatedLogContext.indexOf("oproto=IMAP") > -1);
 }

 @Test
 void testNonTrustedIPLogString() throws Exception {
  HashMap<String, String> headers = new HashMap<String, String>();
  headers.put(RemoteIP.X_ORIGINATING_IP_HEADER, "172.16.150.11");
  headers.put(RemoteIP.X_ORIGINATING_PORT_HEADER, "8080");
  headers.put(RemoteIP.X_ORIGINATING_PROTOCOL_HEADER, "IMAP");
  MockHttpServletRequest req = new MockHttpServletRequest("test".getBytes("UTF-8"), new URL(
    "http://localhost:7070/service/FooRequest"), "", 80, "10.10.1.1", headers);
  RemoteIP remoteIp = new RemoteIP(req, new RemoteIP.TrustedIPs(new String[]{"192.168.1.1"}));
  remoteIp.addToLoggingContext();
  String updatedLogContext = ZimbraLog.getContextString();
  // we should ignore X-Forwarded-XXX headers from non-trusted clients
  assertEquals(updatedLogContext.indexOf("oip=172.16.150.11"), -1);
  assertEquals(updatedLogContext.indexOf("oport=8080"), -1);
  assertTrue(updatedLogContext.indexOf("ip=10.10.1.1") > -1);
  assertTrue(updatedLogContext.indexOf("port=80") > -1);
  assertEquals(updatedLogContext.indexOf("oproto=IMAP"), -1);
 }
}

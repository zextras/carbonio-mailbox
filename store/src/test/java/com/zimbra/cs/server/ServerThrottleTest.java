// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Sets;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


class ServerThrottleTest {

  String ip = "146.126.106.1";
  String acctId = "abc-123";

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @AfterEach
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  public void throttleIpCount() {
    int numReqs = 100;
    ServerThrottle throttle = new ServerThrottle("test");
    throttle.setIpReqsPerSecond(numReqs);
    long time = System.currentTimeMillis() + 10000000;
    // add timestamps far enough in the future that they don't get pruned.
    // this tests basic functions

    assertFalse(throttle.isIpThrottled(ip));

    for (int i = 0; i < numReqs; i++) {
      throttle.addIpReq(ip, time);
    }

    assertTrue(throttle.isIpThrottled(ip));
    assertFalse(throttle.isIpThrottled(ip + "foo"));
    assertFalse(throttle.isAccountThrottled(acctId));
  }

  @Test
  void throttleIpIgnore() {
    int numReqs = 100;
    ServerThrottle throttle = new ServerThrottle("test");
    throttle.setIpReqsPerSecond(numReqs);
    long time = System.currentTimeMillis() + 10000000;
    assertFalse(throttle.isIpThrottled(ip));

    for (int i = 0; i < numReqs; i++) {
      throttle.addIpReq(ip, time);
    }

    assertTrue(throttle.isIpThrottled(ip));
    throttle.addIgnoredIp(ip);
    assertFalse(throttle.isIpThrottled(ip));
  }

  @Test
  void throttleIpTime() {
    int numReqs = 1;
    ServerThrottle throttle = new ServerThrottle("test");
    throttle.setIpReqsPerSecond(numReqs);

    assertFalse(throttle.isIpThrottled(ip));
    // on a really slow system this might fail; if there is more than 1
    // second pause in execution here
    assertTrue(throttle.isIpThrottled(ip));
    assertFalse(throttle.isIpThrottled(ip + "foo"));
    assertFalse(throttle.isAccountThrottled(acctId));

    try {
      Thread.sleep(1001);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    assertFalse(throttle.isIpThrottled(ip));
    assertFalse(throttle.isIpThrottled(ip + "foo"));
    assertFalse(throttle.isAccountThrottled(acctId));
  }

  @Test
  void throttleAcctCount() {
    int numReqs = 100;
    ServerThrottle throttle = new ServerThrottle("test");
    throttle.setAcctReqsPerSecond(numReqs);
    long time = System.currentTimeMillis() + 10000000;
    // add timestamps far enough in the future that they don't get pruned.
    // this tests basic functions

    assertFalse(throttle.isAccountThrottled(acctId));

    for (int i = 0; i < numReqs; i++) {
      throttle.addAcctReq(acctId, time);
    }

    assertTrue(throttle.isAccountThrottled(acctId));
    assertFalse(throttle.isAccountThrottled(acctId + "foo"));
    assertFalse(throttle.isIpThrottled(ip));
  }

  @Test
  void throttleAcctTime() {
    int numReqs = 1;
    ServerThrottle throttle = new ServerThrottle("test");
    throttle.setAcctReqsPerSecond(numReqs);

    assertFalse(throttle.isAccountThrottled(acctId));
    // on a really slow system this might fail; if there is more than 1
    // second pause in execution here
    assertTrue(throttle.isAccountThrottled(acctId));
    assertFalse(throttle.isAccountThrottled(acctId + "foo"));
    assertFalse(throttle.isIpThrottled(ip));

    try {
      Thread.sleep(1001);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    assertFalse(throttle.isAccountThrottled(acctId));
    assertFalse(throttle.isAccountThrottled(acctId + "foo"));
    assertFalse(throttle.isIpThrottled(ip));
  }

  @Test
  void throttleIpWhitelist() {
    int numReqs = 100;
    ServerThrottle throttle = new ServerThrottle("test");
    throttle.setIpReqsPerSecond(numReqs);
    long time = System.currentTimeMillis() + 10000000;
    assertFalse(throttle.isIpThrottled(ip));

    for (int i = 0; i < numReqs; i++) {
      throttle.addIpReq(ip, time);
    }

    assertTrue(throttle.isIpThrottled(ip));
    throttle.addWhitelistIp(ip);
    assertFalse(throttle.isIpThrottled(ip));
  }

  @Test
  void throttleAcctWhitelist() {
    int numReqs = 100;
    ServerThrottle throttle = new ServerThrottle("test");
    throttle.setAcctReqsPerSecond(numReqs);
    long time = System.currentTimeMillis() + 10000000;
    // add timestamps far enough in the future that they don't get pruned.
    // this tests basic functions

    assertFalse(throttle.isAccountThrottled(acctId, ip));

    for (int i = 0; i < numReqs; i++) {
      throttle.addAcctReq(acctId, time);
    }

    //add null to ip list just to be sure varargs is handled the way it should be
    assertTrue(throttle.isAccountThrottled(acctId, ip, null));
    assertTrue(throttle.isAccountThrottled(acctId, ip));
    assertTrue(throttle.isAccountThrottled(acctId, null, ip));
    assertTrue(throttle.isAccountThrottled(acctId, null, null, ip));
    assertFalse(throttle.isIpThrottled(ip));

    throttle.addWhitelistIp(ip);
    //add null to ip list just to be sure varargs is handled the way it should be
    assertFalse(throttle.isAccountThrottled(acctId, null, ip));
    assertFalse(throttle.isAccountThrottled(acctId, ip));
    assertFalse(throttle.isAccountThrottled(acctId, ip, null));
    assertFalse(throttle.isAccountThrottled(acctId, null, null, ip));
  }

  @Test
  void testUnknownHost() {
    try {
      ServerThrottle.configureThrottle("IMAP", 1, 1,
          Sets.newHashSet("nosuchhost", "www.zextras.com"), new HashSet<>());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

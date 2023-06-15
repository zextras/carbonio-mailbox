// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.rules.MethodRule;

import com.google.common.collect.Maps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning.PrefExternalSendersType;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.util.ZTestWatchman;

public class NotificationTest {
    
     public String testName;
    @Rule public MethodRule watchman = new ZTestWatchman();

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        Map<String, Object> attrs = Maps.newHashMap();

        attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
        prov.createAccount("testZCS3546@zimbra.com", "secret", attrs);
    }

 @BeforeEach
 public void setUp(TestInfo testInfo) throws Exception {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
  System.out.println( testName);
  MailboxTestUtil.clearData();
 }

    @AfterEach
    public void tearDown() throws Exception {
        MailboxTestUtil.clearData();

    }

 @Test
 void testOOOWhenSpecificDomainSenderNotSet() throws Exception {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "testZCS3546@zimbra.com");
  acct1.setPrefOutOfOfficeSuppressExternalReply(true);
  acct1.unsetInternalSendersDomain();
  acct1.unsetPrefExternalSendersType();
  Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
  boolean skipOOO = Notification.skipOutOfOfficeMsg("test3@synacor.com", acct1, mbox1);
  assertEquals(true, skipOOO);
 }

 @Test
 void testOOOWhenSpecificDomainSenderIsSet() throws Exception {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "testZCS3546@zimbra.com");
  acct1.setPrefOutOfOfficeSuppressExternalReply(true);
  acct1.setPrefExternalSendersType(PrefExternalSendersType.INSD);
  String[] domains = {"synacor.com"};
  acct1.setPrefOutOfOfficeSpecificDomains(domains);
  Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
  boolean skipOOO = Notification.skipOutOfOfficeMsg("test3@synacor.com", acct1, mbox1);
  assertEquals(false, skipOOO);
 }

 @Test
 void testOOOMsgWhenSpecificDomainSenderIsSetWithSpecificDomainSender() throws Exception {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "testZCS3546@zimbra.com");
  acct1.setPrefOutOfOfficeExternalReplyEnabled(true);
  acct1.setPrefExternalSendersType(PrefExternalSendersType.INSD);
  String[] domains = {"synacor.com"};
  acct1.setPrefOutOfOfficeSpecificDomains(domains);
  acct1.setInternalSendersDomain(domains);
  Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
  boolean customMsg = Notification.sendOutOfOfficeExternalReply("test3@synacor.com", acct1, mbox1);
  assertEquals(true, customMsg);
 }

 @Test
 void testOOOMsgWhenSpecificDomainSenderIsSetWithInternalSender() throws Exception {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "testZCS3546@zimbra.com");
  acct1.setPrefOutOfOfficeExternalReplyEnabled(true);
  acct1.setPrefExternalSendersType(PrefExternalSendersType.INSD);
  String[] domains = {"synacor.com"};
  acct1.setPrefOutOfOfficeSpecificDomains(domains);
  Mailbox mbox1 = MailboxManager.getInstance().getMailboxByAccount(acct1);
  boolean customMsg = Notification.sendOutOfOfficeExternalReply("test2@zimbra.com", acct1, mbox1);
  assertEquals(false, customMsg);
 }
}

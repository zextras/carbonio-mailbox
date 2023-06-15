// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning.FeatureAddressVerificationStatus;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.MailboxData;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.account.ModifyPrefs;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.ModifyPrefsRequest;
import com.zimbra.soap.account.type.Pref;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import org.junit.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

public class ModifyPrefsTest {

    public static String zimbraServerDir = "";

    
    public String testName;
    @Rule
    public MethodRule watchman = new TestWatchman() {
        @Override
        public void failed(Throwable e, FrameworkMethod method) {
            System.out.println(method.getName() + " " + e.getClass().getSimpleName());
        }
    };

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();

        Map<String, Object> attrs = Maps.newHashMap();
        prov.createDomain("zimbra.com", attrs);

        attrs = Maps.newHashMap();
        prov.createAccount("test@zimbra.com", "secret", attrs);

        MailboxManager.setInstance(new MailboxManager() {

            @Override
            protected Mailbox instantiateMailbox(MailboxData data) {
                return new Mailbox(data) {

                    @Override
                    public MailSender getMailSender() {
                        return new MailSender() {

                            @Override
                            protected Collection<Address> sendMessage(Mailbox mbox, MimeMessage mm,
                                Collection<RollbackData> rollbacks)
                                throws SafeMessagingException, IOException {
                                try {
                                    return Arrays.asList(getRecipients(mm));
                                } catch (Exception e) {
                                    return Collections.emptyList();
                                }
                            }
                        };
                    }
                };
            }
        });

        L10nUtil.setMsgClassLoader("../store-conf/conf/msgs");
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
 void testMsgMaxAttr() throws Exception {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct1);
  acct1.setFeatureMailForwardingEnabled(true);
  acct1.setFeatureAddressVerificationEnabled(true);
  assertNull(acct1.getPrefMailForwardingAddress());
  assertNull(acct1.getFeatureAddressUnderVerification());
  ModifyPrefsRequest request = new ModifyPrefsRequest();
  Pref pref = new Pref(Provisioning.A_zimbraPrefMailForwardingAddress,
    "test1@somedomain.com");
  request.addPref(pref);
  Element req = JaxbUtil.jaxbToElement(request);
  new ModifyPrefs().handle(req, ServiceTestUtil.getRequestContext(mbox.getAccount()));
  /*
   * Verify that the forwarding address is not directly stored into
   * 'zimbraPrefMailForwardingAddress' Instead, it is stored in
   * 'zimbraFeatureAddressUnderVerification' till the time it
   * gets verification
    */
  assertNull(acct1.getPrefMailForwardingAddress());
  assertEquals("test1@somedomain.com",
    acct1.getFeatureAddressUnderVerification());
  /*
   * disable the verification feature and check that the forwarding
   * address is directly stored into 'zimbraPrefMailForwardingAddress'
    */
  acct1.setPrefMailForwardingAddress(null);
  acct1.setFeatureAddressUnderVerification(null);
  acct1.setFeatureAddressVerificationEnabled(false);
  new ModifyPrefs().handle(req, ServiceTestUtil.getRequestContext(mbox.getAccount()));
  assertNull(acct1.getFeatureAddressUnderVerification());
  assertEquals("test1@somedomain.com", acct1.getPrefMailForwardingAddress());
  assertEquals(FeatureAddressVerificationStatus.pending, acct1.getFeatureAddressVerificationStatus());
 }

 @Test
 void testPrefCalendarInitialViewYear() throws Exception {
  Account acct1 = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct1);
  ModifyPrefsRequest request = new ModifyPrefsRequest();
  Pref pref = new Pref(Provisioning.A_zimbraPrefCalendarInitialView, "year");
  request.addPref(pref);
  Element req = JaxbUtil.jaxbToElement(request);
  new ModifyPrefs().handle(req, ServiceTestUtil.getRequestContext(mbox.getAccount()));
  new ModifyPrefs().handle(req, ServiceTestUtil.getRequestContext(mbox.getAccount()));
  assertFalse(acct1.getPrefCalendarInitialView().isDay());
  assertTrue(acct1.getPrefCalendarInitialView().isYear());
 }
}

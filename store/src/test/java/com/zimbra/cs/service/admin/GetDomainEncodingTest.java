// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.rules.MethodRule;

import com.google.common.collect.Maps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.cs.util.ZTestWatchman;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;

public class GetDomainEncodingTest {

    
    public String testName;
    @Rule
    public MethodRule watchman = new ZTestWatchman();

 @BeforeEach
 public void setUp(TestInfo testInfo) throws Exception {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
  System.out.println( testName);
  MailboxTestUtil.initServer();
  MailboxTestUtil.clearData();
  Provisioning prov = Provisioning.getInstance();
  Map<String, Object> attrs = Maps.newHashMap();
  String[] values = new String[2];
  values[0] = "ldap://ldap1.com";
  values[1] = "ldap://ldap2.com";
  attrs.put("zimbraAuthLdapURL", values);
  prov.createDomain("zimbra.com", attrs);
  attrs = Maps.newHashMap();
  attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
  prov.createAccount("test201@zimbra.com", "secret", attrs);
 }

 @Test
 void testZBUG201() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("test201@zimbra.com");
  Domain domain = Provisioning.getInstance().getDomain(acct);
  Map<String, Object> context = ServiceTestUtil.getRequestContext(acct);
  ZimbraSoapContext zsc = (ZimbraSoapContext) context.get(SoapEngine.ZIMBRA_CONTEXT);
  Element response = zsc.createElement(AdminConstants.GET_DOMAIN_RESPONSE);
  GetDomain.encodeDomain(response, domain, true, null, null);
  // check that the response contains single space separated value for zimbraAuthLdapURL
  assertEquals(true, response.prettyPrint().contains("<a n=\"zimbraAuthLdapURL\">ldap://ldap1.com ldap://ldap2.com</a>"));
 }

    @AfterEach
    public void tearDown() {
        try {
            MailboxTestUtil.clearData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
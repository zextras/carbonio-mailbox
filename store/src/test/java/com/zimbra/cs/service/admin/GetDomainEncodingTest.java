// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.google.common.collect.Maps;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.cs.util.ZTestWatchman;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;
import java.util.UUID;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestName;

public class GetDomainEncodingTest {

  @Rule public TestName testName = new TestName();
  @Rule public MethodRule watchman = new ZTestWatchman();

  @Before
  public void setUp() throws Exception {
    System.out.println(testName.getMethodName());
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
  public void testZBUG201() throws Exception {
    Account acct = Provisioning.getInstance().getAccountByName("test201@zimbra.com");
    Domain domain = Provisioning.getInstance().getDomain(acct);
    Map<String, Object> context = ServiceTestUtil.getRequestContext(acct);
    ZimbraSoapContext zsc = (ZimbraSoapContext) context.get(SoapEngine.ZIMBRA_CONTEXT);
    Element response = zsc.createElement(AdminConstants.GET_DOMAIN_RESPONSE);
    GetDomain.encodeDomain(response, domain, true, null, null);
    // check that the response contains single space separated value for zimbraAuthLdapURL
    Assert.assertEquals(
        true,
        response
            .prettyPrint()
            .contains("<a n=\"zimbraAuthLdapURL\">ldap://ldap1.com ldap://ldap2.com</a>"));
  }

  @After
  public void tearDown() {
    try {
      MailboxTestUtil.clearData();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

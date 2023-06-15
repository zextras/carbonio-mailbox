// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;


public class AutoCompleteTest {

    
    public String testName;

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
  prov.createDomain("zimbra.com", attrs);

  attrs = Maps.newHashMap();
  attrs.put(Provisioning.A_zimbraId, UUID.randomUUID().toString());
  prov.createAccount("test3951@zimbra.com", "secret", attrs);
 }

 @Test
 void test3951() throws Exception {
  Account acct = Provisioning.getInstance().get(Key.AccountBy.name, "test3951@zimbra.com");
  Element request = new Element.XMLElement(MailConstants.AUTO_COMPLETE_REQUEST);
  request.addAttribute("name", " ");
  boolean exceptionThrown;
  try {
   new AutoComplete().handle(request, ServiceTestUtil.getRequestContext(acct));
   exceptionThrown = false;
  } catch (ServiceException e) {
   exceptionThrown = true;
   assertEquals("invalid request: name parameter is empty", e.getMessage());
  }
  assertEquals(true, exceptionThrown);
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

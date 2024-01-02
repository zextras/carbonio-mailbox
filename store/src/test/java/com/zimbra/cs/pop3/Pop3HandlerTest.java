// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.pop3;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;


import com.zimbra.cs.account.Account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.server.ServerThrottle;



public class Pop3HandlerTest {
    private static final String LOCAL_USER = "localpoptest@zimbra.com";

     public String testName;
    

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        String[] hosts = {"localhost", "127.0.0.1"};
        ServerThrottle.configureThrottle(new Pop3Config(false).getProtocol(), 100, 100, Arrays.asList(hosts), Arrays.asList(hosts));
    }

 @BeforeEach
 public void setUp(TestInfo testInfo) throws Exception {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
  System.out.println( testName);
  Provisioning prov = Provisioning.getInstance();
  HashMap<String, Object> attrs = new HashMap<String, Object>();
  attrs.put(Provisioning.A_zimbraId, "12aa345b-2b47-44e6-8cb8-7fdfa18c1a9f");
  prov.createAccount(LOCAL_USER, "secret", attrs);
 }

    @AfterEach
    public void tearDown() throws Exception {
        MailboxTestUtil.clearData();
    }

 @Test
 void testLogin3() throws Exception {
  Account acct = Provisioning.getInstance().getAccount("12aa345b-2b47-44e6-8cb8-7fdfa18c1a9f");
  Pop3Handler handler = new MockPop3Handler();

  acct.setPop3Enabled(true);
  acct.setPrefPop3Enabled(true);
  handler.authenticate(LOCAL_USER, null, "secret", null);
  assertEquals(Pop3Handler.STATE_TRANSACTION, handler.state);
 }

 @Test
 void testLogin4() throws Exception {
  assertThrows(Pop3CmdException.class, () -> {
   Account acct = Provisioning.getInstance().getAccount("12aa345b-2b47-44e6-8cb8-7fdfa18c1a9f");
   Pop3Handler handler = new MockPop3Handler();

   acct.setPop3Enabled(true);
   acct.setPrefPop3Enabled(false);
   handler.authenticate(LOCAL_USER, null, "secret", null);
  });
 }

 @Test
 void testLogin7() throws Exception {
  assertThrows(Pop3CmdException.class, () -> {
   Account acct = Provisioning.getInstance().getAccount("12aa345b-2b47-44e6-8cb8-7fdfa18c1a9f");
   Pop3Handler handler = new MockPop3Handler();

   acct.setPop3Enabled(false);
   acct.setPrefPop3Enabled(true);
   handler.authenticate(LOCAL_USER, null, "secret", null);
  });
 }

 @Test
 void testLogin8() throws Exception {
  assertThrows(Pop3CmdException.class, () -> {
   Account acct = Provisioning.getInstance().getAccount("12aa345b-2b47-44e6-8cb8-7fdfa18c1a9f");
   Pop3Handler handler = new MockPop3Handler();

   acct.setPop3Enabled(false);
   acct.setPrefPop3Enabled(false);
   handler.authenticate(LOCAL_USER, null, "secret", null);
  });
 }
}


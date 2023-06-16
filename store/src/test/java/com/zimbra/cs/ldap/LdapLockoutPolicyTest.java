// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3

package com.zimbra.cs.ldap;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapLockoutPolicy;
import com.zimbra.cs.mailbox.MailboxTestUtil;

public class LdapLockoutPolicyTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        MockProvisioning prov = new MockProvisioning();
        Provisioning.setInstance(prov);

        Map<String, Object> attrs = Maps.newHashMap();
        prov.createDomain("zimbra.com", attrs);

        attrs = Maps.newHashMap();
        prov.createAccount("test@zimbra.com", "secret", attrs);
    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 /**
  * Test method for
  * {@link com.zimbra.cs.account.ldap.LdapLockoutPolicy#failedLogin()}.
  */
 @Test
 void testFailedLogin() {
  Account acct;
  try {
   acct = Provisioning.getInstance().get(Key.AccountBy.name, "test@zimbra.com");
   acct.setPasswordLockoutEnabled(true);
   acct.setPasswordLockoutMaxFailures(2);
   acct.setPasswordLockoutFailureLifetime("120s");

   // First failure
   LdapLockoutPolicy lockoutPolicy = new LdapLockoutPolicy(Provisioning.getInstance(), acct);
   lockoutPolicy.failedLogin();
   // failure time is updated
   assertEquals(1, acct.getPasswordLockoutFailureTimeAsString().length);

   // second Failure
   lockoutPolicy = new LdapLockoutPolicy(Provisioning.getInstance(), acct);
   lockoutPolicy.failedLogin();
   String[] failureTime = acct.getPasswordLockoutFailureTimeAsString();
   // failure time is updated
   assertEquals(2, failureTime.length);

   // account should be locked after two failure attempts
   assertTrue(acct.getAccountStatus().isLockout());

   // Third failure
   lockoutPolicy = new LdapLockoutPolicy(Provisioning.getInstance(), acct);
   lockoutPolicy.failedLogin();

   // Third failure attempt should not update failure time
   assertEquals(failureTime[0], acct.getPasswordLockoutFailureTimeAsString()[0]);
   assertEquals(failureTime[1], acct.getPasswordLockoutFailureTimeAsString()[1]);
  } catch (Exception e) {
   fail("No expcetion should be thrown" + e);
  }
 }
}

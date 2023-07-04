// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.zimbra.common.account.Key.AccountBy;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import com.zimbra.cs.mailbox.MailboxTestUtil;

/**
 * Unit test for {@link ZimbraAuthToken}.
 *
 * @author ysasaki
 */
public class ZimbraAuthTokenTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initProvisioning();
        Provisioning.getInstance().createAccount("user1@example.zimbra.com", "secret", new HashMap<String, Object>());
    }

 @Test
 void test() throws Exception {
  Account a = Provisioning.getInstance().get(AccountBy.name, "user1@example.zimbra.com");
  ZimbraAuthToken at = new ZimbraAuthToken(a);
  long start = System.currentTimeMillis();
  String encoded = at.getEncoded();
  for (int i = 0; i < 1000; i++) {
   new ZimbraAuthToken(encoded);
  }
  System.out.println("Encoded 1000 auth-tokens elapsed=" + (System.currentTimeMillis() - start));

  start = System.currentTimeMillis();
  for (int i = 0; i < 1000; i++) {
   ZimbraAuthToken.getAuthToken(encoded);
  }
  System.out.println("Decoded 1000 auth-tokens elapsed=" + (System.currentTimeMillis() - start));
 }

 @Test
 void testEncodedDifferentOnTokenIDReset() throws Exception {
  Account a = Provisioning.getInstance().get(AccountBy.name, "user1@example.zimbra.com");
  ZimbraAuthToken at = new ZimbraAuthToken(a);
  ZimbraAuthToken clonedAuthToken = at.clone();
  clonedAuthToken.resetTokenId();
  assertNotEquals(at.getEncoded(), clonedAuthToken.getEncoded());
 }

}

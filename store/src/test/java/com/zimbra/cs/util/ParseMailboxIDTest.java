// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.zimbra.common.localconfig.LC;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.util.ParseMailboxID;

/**
 * Unit test for {@link ParseMailboxID}.
 *
 * @author ysasaki
 */
public class ParseMailboxIDTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        MockProvisioning prov = new MockProvisioning();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
        Provisioning.setInstance(prov);
    }

 @Test
 void parseLocalMailboxId() throws Exception {
  ParseMailboxID id = ParseMailboxID.parse("1");
  assertTrue(id.isLocal());
  assertNull(id.getServer());
  assertEquals(1, id.getMailboxId());
  assertFalse(id.isAllMailboxIds());
  assertFalse(id.isAllServers());
  assertNull(id.getAccount());
 }

 @Test
 void parseEmail() throws Exception {
  ParseMailboxID id = ParseMailboxID.parse("test@zimbra.com");
  assertTrue(id.isLocal());
  assertEquals("localhost", id.getServer());
  assertEquals(0, id.getMailboxId());
  assertFalse(id.isAllMailboxIds());
  assertFalse(id.isAllServers());
  assertEquals(Provisioning.getInstance().getAccountByName("test@zimbra.com"), id.getAccount());
 }

 @Test
 void parseAccountId() throws Exception {
  ParseMailboxID id = ParseMailboxID.parse(MockProvisioning.DEFAULT_ACCOUNT_ID);
  assertTrue(id.isLocal());
  assertEquals("localhost", id.getServer());
  assertEquals(0, id.getMailboxId());
  assertFalse(id.isAllMailboxIds());
  assertFalse(id.isAllServers());
  assertEquals(Provisioning.getInstance().getAccountByName("test@zimbra.com"), id.getAccount());
 }

 @Test
 void parseMailboxId() throws Exception {
  ParseMailboxID id = ParseMailboxID.parse("/localhost/1");
  assertTrue(id.isLocal());
  assertEquals("localhost", id.getServer());
  assertEquals(1, id.getMailboxId());
  assertFalse(id.isAllMailboxIds());
  assertFalse(id.isAllServers());
  assertNull(id.getAccount());

  try {
   ParseMailboxID.parse("localhost*/3");
   fail();
  } catch (ServiceException expected) {
  }
 }

 @Test
 void parseAllServers() throws Exception {
  ParseMailboxID id = ParseMailboxID.parse("*");
  assertFalse(id.isLocal());
  assertEquals("*", id.getServer());
  assertEquals(0, id.getMailboxId());
  assertTrue(id.isAllMailboxIds());
  assertTrue(id.isAllServers());
  assertNull(id.getAccount());
 }

 @Test
 void parseAllMailboxes() throws Exception {
  ParseMailboxID id = ParseMailboxID.parse("/localhost/*");
  assertTrue(id.isLocal());
  assertEquals("localhost", id.getServer());
  assertEquals(0, id.getMailboxId());
  assertTrue(id.isAllMailboxIds());
  assertFalse(id.isAllServers());
  assertNull(id.getAccount());
 }

}

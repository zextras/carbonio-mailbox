// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.service.util.ParseMailboxID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ParseMailboxID}.
 *
 * @author ysasaki
 */
public class ParseMailboxIDTest extends MailboxTestSuite {
 private Account account;

 @BeforeEach
 public void init() throws Exception {
  account = createAccount().create();
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
  ParseMailboxID id = ParseMailboxID.parse(account.getName());
  assertTrue(id.isLocal());
  assertEquals("localhost", id.getServer());
  assertEquals(0, id.getMailboxId());
  assertFalse(id.isAllMailboxIds());
  assertFalse(id.isAllServers());
  assertEquals(account.toString(), id.getAccount().toString());
 }

 @Test
 void parseAccountId() throws Exception {
  ParseMailboxID id = ParseMailboxID.parse(account.getId());
  assertTrue(id.isLocal());
  assertEquals("localhost", id.getServer());
  assertEquals(0, id.getMailboxId());
  assertFalse(id.isAllMailboxIds());
  assertFalse(id.isAllServers());
  assertEquals(account.toString(), id.getAccount().toString());
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

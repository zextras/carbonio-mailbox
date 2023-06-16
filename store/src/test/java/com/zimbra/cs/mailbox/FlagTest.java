// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zimbra.client.ZItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;

/**
 * Unit test for {@link Flag}.
 *
 * @author ysasaki
 */
public final class FlagTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    @BeforeEach
    public void setUp() throws Exception {
        MailboxTestUtil.clearData();
    }

 @SuppressWarnings("deprecation")
 @Test
 void id() {
  assertEquals(-1, Flag.ID_FROM_ME);
  assertEquals(-2, Flag.ID_ATTACHED);
  assertEquals(-3, Flag.ID_REPLIED);
  assertEquals(-4, Flag.ID_FORWARDED);
  assertEquals(-5, Flag.ID_COPIED);
  assertEquals(-6, Flag.ID_FLAGGED);
  assertEquals(-7, Flag.ID_DRAFT);
  assertEquals(-8, Flag.ID_DELETED);
  assertEquals(-9, Flag.ID_NOTIFIED);
  assertEquals(-10, Flag.ID_UNREAD);
  assertEquals(-11, Flag.ID_HIGH_PRIORITY);
  assertEquals(-12, Flag.ID_LOW_PRIORITY);
  assertEquals(-13, Flag.ID_VERSIONED);
  assertEquals(-14, Flag.ID_INDEXING_DEFERRED);
  assertEquals(-15, Flag.ID_POPPED);
  assertEquals(-16, Flag.ID_NOTE);
  assertEquals(-17, Flag.ID_PRIORITY);
  assertEquals(-18, Flag.ID_POST);
  assertEquals(-20, Flag.ID_SUBSCRIBED);
  assertEquals(-21, Flag.ID_EXCLUDE_FREEBUSY);
  assertEquals(-22, Flag.ID_CHECKED);
  assertEquals(-23, Flag.ID_NO_INHERIT);
  assertEquals(-24, Flag.ID_INVITE);
  assertEquals(-25, Flag.ID_SYNCFOLDER);
  assertEquals(-26, Flag.ID_SYNC);
  assertEquals(-27, Flag.ID_NO_INFERIORS);
  assertEquals(-28, Flag.ID_ARCHIVED);
  assertEquals(-29, Flag.ID_GLOBAL);
  assertEquals(-30, Flag.ID_IN_DUMPSTER);
  assertEquals(-31, Flag.ID_UNCACHED);
 }

 @SuppressWarnings("deprecation")
 @Test
 void bitmask() {
  assertEquals(1, Flag.BITMASK_FROM_ME);
  assertEquals(2, Flag.BITMASK_ATTACHED);
  assertEquals(4, Flag.BITMASK_REPLIED);
  assertEquals(8, Flag.BITMASK_FORWARDED);
  assertEquals(16, Flag.BITMASK_COPIED);
  assertEquals(32, Flag.BITMASK_FLAGGED);
  assertEquals(64, Flag.BITMASK_DRAFT);
  assertEquals(128, Flag.BITMASK_DELETED);
  assertEquals(256, Flag.BITMASK_NOTIFIED);
  assertEquals(512, Flag.BITMASK_UNREAD);
  assertEquals(1024, Flag.BITMASK_HIGH_PRIORITY);
  assertEquals(2048, Flag.BITMASK_LOW_PRIORITY);
  assertEquals(4096, Flag.BITMASK_VERSIONED);
  assertEquals(8192, Flag.BITMASK_INDEXING_DEFERRED);
  assertEquals(16384, Flag.BITMASK_POPPED);
  assertEquals(32768, Flag.BITMASK_NOTE);
  assertEquals(65536, Flag.BITMASK_PRIORITY);
  assertEquals(131072, Flag.BITMASK_POST);
  assertEquals(524288, Flag.BITMASK_SUBSCRIBED);
  assertEquals(1048576, Flag.BITMASK_EXCLUDE_FREEBUSY);
  assertEquals(2097152, Flag.BITMASK_CHECKED);
  assertEquals(4194304, Flag.BITMASK_NO_INHERIT);
  assertEquals(8388608, Flag.BITMASK_INVITE);
  assertEquals(16777216, Flag.BITMASK_SYNCFOLDER);
  assertEquals(33554432, Flag.BITMASK_SYNC);
  assertEquals(67108864, Flag.BITMASK_NO_INFERIORS);
  assertEquals(134217728, Flag.BITMASK_ARCHIVED);
  assertEquals(268435456, Flag.BITMASK_GLOBAL);
  assertEquals(536870912, Flag.BITMASK_IN_DUMPSTER);
  assertEquals(1073741824, Flag.BITMASK_UNCACHED);
 }

 @Test
 void equals() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  assertEquals(Flag.FlagInfo.UNREAD.toFlag(mbox), Flag.FlagInfo.UNREAD.toFlag(mbox));
  assertEquals(Flag.FlagInfo.UNREAD.toFlag(mbox).hashCode(), Flag.FlagInfo.UNREAD.toFlag(mbox).hashCode());
 }

 @Test
 void unique() throws Exception {
  Set<Character> seen = new HashSet<Character>();
  for (Flag.FlagInfo finfo : Flag.FlagInfo.values()) {
   if (!finfo.isHidden()) {
    assertFalse(seen.contains(finfo.ch), "have not yet seen " + finfo.ch);
    seen.add(finfo.ch);
   }
  }
 }
}

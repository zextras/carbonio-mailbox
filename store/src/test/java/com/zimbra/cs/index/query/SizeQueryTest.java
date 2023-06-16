// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import java.text.ParseException;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.zimbra.cs.account.MockProvisioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailboxTestUtil;

/**
 * Unit test for {@link SizeQuery}.
 *
 * @author ysasaki
 */
public final class SizeQueryTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        MockProvisioning prov = new MockProvisioning();
        prov.createAccount("zero@zimbra.com", "secret", new HashMap<String, Object>());
        Provisioning.setInstance(prov);
    }

 @Test
 void parseSize() throws Exception {
  SizeQuery query = new SizeQuery(SizeQuery.Type.EQ, "1KB");
  assertEquals("Q(SIZE:=1024)", query.toString());

  query = new SizeQuery(SizeQuery.Type.EQ, ">1KB");
  assertEquals("Q(SIZE:>1024)", query.toString());

  query = new SizeQuery(SizeQuery.Type.EQ, "<1KB");
  assertEquals("Q(SIZE:<1024)", query.toString());

  query = new SizeQuery(SizeQuery.Type.EQ, ">=1KB");
  assertEquals("Q(SIZE:>1023)", query.toString());

  query = new SizeQuery(SizeQuery.Type.EQ, "<=1KB");
  assertEquals("Q(SIZE:<1025)", query.toString());

  query = new SizeQuery(SizeQuery.Type.EQ, "1 KB");
  assertEquals("Q(SIZE:=1024)", query.toString());

  try {
   query = new SizeQuery(SizeQuery.Type.EQ, "x KB");
   fail();
  } catch (ParseException expected) {
  }
 }

}

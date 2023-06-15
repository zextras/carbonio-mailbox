// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.zimbra.common.service.ServiceException;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.account.MockServer;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.imap.ImapLoadBalancingMechanism.AccountIdHashMechanism;
import com.zimbra.cs.imap.ImapLoadBalancingMechanism.CustomLBMech;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.service.MockHttpServletRequest;

/**
 * Unit test for {@link ImapLoadBalancingMechanism}.
 *
 */
public final class ImapLoadBalancingMechanismTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

 @Test
 void accountIdHashMechanismEmptyServerList()
   throws Exception
 {
  try {
   ImapLoadBalancingMechanism mech = ImapLoadBalancingMechanism.newInstance(
     ImapLoadBalancingMechanism.ImapLBMech.AccountIdHash.name());
   ArrayList<Server> pool = new ArrayList<Server>();
   mech.getImapServerFromPool(null, "dummyAccountId", pool);
   fail("should have raised ServiceException");
  } catch (ServiceException se) {
   /* this is what we expect */
  } catch (Exception e) {
   fail(String.format("Unexpected exception thrown %s", e.getMessage()));
  }
 }

 @Test
 void accountIdHashMechanismHashFromPool()
   throws Exception
 {
  ImapLoadBalancingMechanism mech = ImapLoadBalancingMechanism.newInstance(
    ImapLoadBalancingMechanism.ImapLBMech.AccountIdHash.name());
  ArrayList<Server> pool = new ArrayList<Server>();
  Server s0 = new MockServer("server-0", "0");
  Server s1 = new MockServer("server-1", "1");
  Server s2 = new MockServer("server-2", "2");
  // Add to pool out-of-order to verify that we are sorting correctly
  pool.add(s1);
  pool.add(s0);
  pool.add(s2);
  HashMap<String, String> headers = new HashMap<String, String>();
  HttpServletRequest req = new MockHttpServletRequest(null, null, null, 123, "127.0.0.1", headers);
  String acctId0 = "79e9a595-c34b-469a-a1ee-e2b9d03f9aa7"; /* hash=1536494685, hash % 3 = 0 */
  String acctId1 = "615922e5-2318-4b8b-8734-d209a99f8252"; /* hash=454270162,  hash % 3 = 1 */
  String acctId2 = "f5c68357-61d9-4658-a7fc-e7273929ca0c"; /* hash=1373626454, hash % 3 = 2 */
  assertEquals(s0, mech.getImapServerFromPool(req, acctId0, pool), "Should have got 0th entry from sorted pool");
  assertEquals(s1, mech.getImapServerFromPool(req, acctId1, pool), "Should have got 1st entry from sorted pool");
  assertEquals(s2, mech.getImapServerFromPool(req, acctId2, pool), "Should have got 2nd entry from sorted pool");
 }

 @Test
 void testCustomLoadBalancingMech() throws Exception {
  CustomLBMech.register("testmech", TestCustomLBMech.class);
  ImapLoadBalancingMechanism mech = CustomLBMech.loadCustomLBMech("custom:testmech foo bar");
  assertNotNull(mech, "Loaded mechanism should be a TestCustomLBMech");
  assertTrue((mech instanceof TestCustomLBMech), String.format("Loaded mechanism '%s' should be a TestCustomLBMech",
    mech.getClass().getName()));
  CustomLBMech customMech = (CustomLBMech) mech;
  assertEquals(customMech.args.get(0), "foo", "Custom Mech arg[0]");
  assertEquals(customMech.args.get(1), "bar", "Custom Mech arg[1]");
  mech = CustomLBMech.loadCustomLBMech("custom:testmech");
  assertNotNull(mech, "2nd Loaded mechanism should be a TestCustomLBMech");
  assertTrue((mech instanceof TestCustomLBMech), String.format("2nd Loaded mechanism '%s' should be a TestCustomLBMech",
    mech.getClass().getName()));
  assertNull(((CustomLBMech) mech).args, "Args for custom mech after 2nd load");
  mech = CustomLBMech.loadCustomLBMech("custom:unregisteredmech");
  assertNotNull(mech, "3rd Loaded mechanism should be AccountIdHashMechanism");
  assertTrue((mech instanceof AccountIdHashMechanism), String.format(
    "Loaded mechanism '%s' when configured bad custom mech should be AccountIdHashMechanism",
    mech.getClass().getName()));
 }

    public static class TestCustomLBMech extends CustomLBMech {

        protected TestCustomLBMech(List<String> args) {
            super(args);
        }

        @Override
        public Server getImapServerFromPool(HttpServletRequest httpReq, String accountID,
                List<Server> pool) throws ServiceException {
            return null;
        }
    }
}

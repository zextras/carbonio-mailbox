// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.session;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SessionMap}.
 *
 * @author ysasaki
 */
public final class SessionMapTest {

 @Test
 void test() throws Exception {
  SessionMap map = new SessionMap(Session.Type.NULL);

  Session s1 = new AdminSession("a1").testSetSessionId("s1");
  Session s12 = new AdminSession("a1").testSetSessionId("s2");
  Session s2 = new AdminSession("a2").testSetSessionId("s1");
  Session s3 = new AdminSession("a3").testSetSessionId("s1");
  Session s4 = new AdminSession("a4").testSetSessionId("s1");
  Session s5 = new AdminSession("a5").testSetSessionId("s1");
  Session s52 = new AdminSession("a5").testSetSessionId("s2");

  map.put("a1", "s1", s1);
  map.put("a1", "s2", s12);
  // sleep ensures the system clock goes fwd, important since
  // we're testing things that work based on last access time
  Thread.sleep(1);
  map.put("a2", "s1", s2);
  Thread.sleep(1);
  long afterA2 = System.currentTimeMillis();
  Thread.sleep(1);
  map.put("a3", "s1", s3);
  map.put("a4", "s1", s4);
  map.put("a5", "s1", s5);
  map.put("a5", "s2", s52);
  Thread.sleep(1);

  assertEquals(5, map.totalActiveAccounts());
  assertEquals(7, map.totalActiveSessions());

  Session check = map.get("a2", "s1");
  assertNotNull(check);
  assertEquals(5, map.totalActiveAccounts());
  assertEquals(7, map.totalActiveSessions());

  // map should be (access-order): a1_s2, s1_s2, a3_s1, ... a2_s1
  List<Session> removed = map.pruneSessionsByTime(afterA2);
  assertFalse(removed.isEmpty());
  boolean hasA1S1 = false;
  boolean hasA1S2 = false;
  boolean hasA2 = false;
  for (Session s : removed) {
   if (s.getAuthenticatedAccountId().equals("a1")) {
    if (s.getSessionId().equals("s1")) {
     hasA1S1 = true;
    }
    if (s.getSessionId().equals("s2")) {
     hasA1S2 = true;
    }
   }
   if (s.getAuthenticatedAccountId().equals("a2")) {
    hasA2 = true;
   }
  }
  assertTrue(hasA1S1);
  assertTrue(hasA1S2);
  assertFalse(hasA2);

  assertEquals(4, map.totalActiveAccounts());
  assertEquals(5, map.totalActiveSessions());

  map.remove("a5", "s1");
  assertEquals(4, map.totalActiveAccounts());
  assertEquals(4, map.totalActiveSessions());

  map.remove("a5", "s2");
  assertEquals(3, map.totalActiveAccounts());
  assertEquals(3, map.totalActiveSessions());

  List<Session> sessions = map.copySessionList();
  assertEquals(3, sessions.size());
  Collections.sort(sessions, new Comparator<Session>() {
   @Override
   public int compare(Session s1, Session s2) {
    String hash1 = s1.getAuthenticatedAccountId() + ':' + s1.getSessionId();
    String hash2 = s2.getAuthenticatedAccountId() + ':' + s2.getSessionId();
    return hash1.compareTo(hash2);
   }
  });
  assertEquals("a2", sessions.get(0).getAuthenticatedAccountId());
  assertEquals("s1", sessions.get(0).getSessionId());
  assertEquals("a3", sessions.get(1).getAuthenticatedAccountId());
  assertEquals("s1", sessions.get(1).getSessionId());
  assertEquals("a4", sessions.get(2).getAuthenticatedAccountId());
  assertEquals("s1", sessions.get(2).getSessionId());
 }
}

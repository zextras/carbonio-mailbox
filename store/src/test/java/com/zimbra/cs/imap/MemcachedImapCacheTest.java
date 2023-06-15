// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.zimbra.common.util.memcached.ZimbraMemcachedClient;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.memcached.MemcachedConnector;
import com.zimbra.cs.util.ZTestWatchman;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Optional;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.rules.MethodRule;

/**
 * @author zimbra
 */
public class MemcachedImapCacheTest {

   public String testName;
  @Rule public MethodRule watchman = new ZTestWatchman();

 /**
  * @throws java.lang.Exception
   */
 @BeforeEach
 public void setUp(TestInfo testInfo) throws Exception {
  Optional<Method> testMethod = testInfo.getTestMethod();
  if (testMethod.isPresent()) {
   this.testName = testMethod.get().getName();
  }
  MailboxTestUtil.initProvisioning("./");
 }

  /**
   * @throws java.lang.Exception
   */
  @AfterEach
  public void tearDown() throws Exception {}

 @Test
 @Disabled("add missing test-timezones-ics file in java-test")
 void testInvalidObject() {
  try {

   mockStatic(MemcachedConnector.class);
   ZimbraMemcachedClient memcachedClient = new MockZimbraMemcachedClient();
   when(MemcachedConnector.getClient()).thenReturn(memcachedClient);
   ImapFolder folder = mock(ImapFolder.class);
   MemcachedImapCache imapCache = new MemcachedImapCache();
   imapCache.put("trash", folder);
   ImapFolder folderDeserz = imapCache.get("trash");
   assertNull(folderDeserz);
  } catch (Exception e) {
   fail("Exception should not be thrown");
  }
 }

  public class MockZimbraMemcachedClient extends ZimbraMemcachedClient {

    @Override
    public Object get(String key) {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      try {
        if (key.equals("zmImap:trash")) {
          ObjectOutputStream oout = new ObjectOutputStream(bout);
          oout.writeObject(new Hacker("hacked"));
          oout.close();
        }
      } catch (Exception e) {
        return bout.toByteArray();
      }
      return bout.toByteArray();
    }

    @Override
    public boolean put(String key, Object value, boolean waitForAck) {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      try (ObjectOutputStream oout = new ObjectOutputStream(bout)) {
        oout.writeObject(new Hacker("hacked"));
      } catch (Exception e) {
        return false;
      }
      return true;
    }
  }
}

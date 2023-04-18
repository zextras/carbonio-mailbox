// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.mailbox.BaseItemInfo;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

public final class MailboxListenerTest {

  private static boolean listenerWasCalled;

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @Before
  public void setup() throws Exception {
    MailboxTestUtil.clearData();
    listenerWasCalled = false;
  }

  @After
  public void cleanup() throws Exception {
    Assert.assertTrue(listenerWasCalled);
    MailboxListener.reset();
  }

  public static class TestListener extends MailboxListener {

    @Override
    public void notify(ChangeNotification notification) {
      listenerWasCalled = true;

      Assert.assertNotNull(notification);
      Assert.assertNotNull(notification.mailboxAccount);
      Assert.assertEquals(notification.mailboxAccount.getId(), MockProvisioning.DEFAULT_ACCOUNT_ID);

      Assert.assertNotNull(notification.mods.created);
      boolean newDocFound = false;
      for (BaseItemInfo item : notification.mods.created.values()) {
        if (item instanceof Document) {
          Document doc = (Document) item;
          if ("test".equals(doc.getName())) newDocFound = true;
        }
      }
      Assert.assertTrue(newDocFound);

      Assert.assertNotNull(notification.mods);
    }
  }
}

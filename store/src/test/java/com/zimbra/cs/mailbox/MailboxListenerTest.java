// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.mailbox.BaseItemInfo;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public final class MailboxListenerTest {

  private static boolean listenerWasCalled;

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @BeforeEach
  public void setup() throws Exception {
    MailboxTestUtil.clearData();
    listenerWasCalled = false;
  }

  @AfterEach
  public void cleanup() throws Exception {
    assertTrue(listenerWasCalled);
    MailboxListener.reset();
  }

  public static class TestListener extends MailboxListener {

    @Override
    public void notify(ChangeNotification notification) {
      listenerWasCalled = true;

      assertNotNull(notification);
      assertNotNull(notification.mailboxAccount);
      assertEquals(notification.mailboxAccount.getId(), MockProvisioning.DEFAULT_ACCOUNT_ID);

      assertNotNull(notification.mods.created);
      boolean newDocFound = false;
      for (BaseItemInfo item : notification.mods.created.values()) {
        if (item instanceof Document) {
          Document doc = (Document) item;
          if ("test".equals(doc.getName())) newDocFound = true;
        }
      }
      assertTrue(newDocFound);

      assertNotNull(notification.mods);
    }
  }
}

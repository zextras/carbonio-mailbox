// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.mailbox.BaseItemInfo;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.session.PendingModifications.Change;
import com.zimbra.cs.session.PendingModifications.ModificationKey;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

  @Test
  public void listenerTest() throws Exception {
    Account acct = Provisioning.getInstance().getAccountById(MockProvisioning.DEFAULT_ACCOUNT_ID);
    OperationContext octxt = new OperationContext(acct);
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
    MailboxListener.register(new TestListener());
    mbox.createDocument(
        octxt,
        Mailbox.ID_FOLDER_BRIEFCASE,
        "test",
        "text/plain",
        "test@zimbra.com",
        "hello",
        new ByteArrayInputStream("hello world".getBytes("UTF-8")));
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
      Change change =
          notification.mods.modified.get(
              new ModificationKey(
                  MockProvisioning.DEFAULT_ACCOUNT_ID, Mailbox.ID_FOLDER_BRIEFCASE));
      Assert.assertNotNull(change);
      Assert.assertEquals(change.why, Change.SIZE);
      Assert.assertNotNull(change.preModifyObj);
      Assert.assertEquals(((Folder) change.preModifyObj).getId(), Mailbox.ID_FOLDER_BRIEFCASE);
    }
  }
}

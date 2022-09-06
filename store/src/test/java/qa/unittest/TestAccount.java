// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.client.LmcSession;
import com.zimbra.cs.client.soap.LmcDeleteAccountRequest;
import com.zimbra.cs.db.DbMailbox;
import com.zimbra.cs.db.DbResults;
import com.zimbra.cs.db.DbUtil;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.volume.VolumeManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 * @author bburtin
 */
public final class TestAccount extends TestCase {

  private static String USER_NAME = "TestAccount";
  private static String PASSWORD = "test123";

  @Override
  public void setUp() throws Exception {
    cleanUp();

    Map<String, Object> attrs = new HashMap<String, Object>();
    attrs.put("zimbraMailHost", LC.zimbra_server_hostname.value());
    attrs.put("cn", "TestAccount");
    attrs.put("displayName", "TestAccount unit test user");
    Provisioning.getInstance().createAccount(TestUtil.getAddress(USER_NAME), PASSWORD, attrs);
  }

  @Override
  public void tearDown() throws Exception {
    cleanUp();
  }

  public void testDeleteAccount() throws Exception {
    ZimbraLog.test.debug("testDeleteAccount()");

    // Get the account and mailbox
    Account account = TestUtil.getAccount(USER_NAME);
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(account);
    String dbName = DbMailbox.getDatabaseName(mbox);
    ZimbraLog.test.debug("Account=" + account.getId() + ", mbox=" + mbox.getId());

    // Confirm that the mailbox database exists
    DbResults results =
        DbUtil.executeQuery("SELECT COUNT(*) FROM mailbox WHERE id = " + mbox.getId());
    assertEquals("Could not find row in mailbox table", 1, results.getInt(1));

    results = DbUtil.executeQuery("SHOW DATABASES LIKE '" + dbName + "'");
    assertEquals("Could not find mailbox database", 1, results.size());

    // Add a message to the account and confirm that the message directory exists
    TestUtil.addMessage(mbox, "TestAccount testDeleteAccount");
    String storePath =
        VolumeManager.getInstance().getCurrentMessageVolume().getMessageRootDir(mbox.getId());
    File storeDir = new File(storePath);
    if (TestUtil.checkLocalBlobs()) {
      assertTrue(storePath + " does not exist", storeDir.exists());
      assertTrue(storePath + " is not a directory", storeDir.isDirectory());
    }
    // Delete the account
    LmcSession session = TestUtil.getAdminSoapSession();
    LmcDeleteAccountRequest req = new LmcDeleteAccountRequest(account.getId());
    req.setSession(session);
    req.invoke(TestUtil.getAdminSoapUrl());

    // Confirm that the mailbox was deleted
    results = DbUtil.executeQuery("SELECT COUNT(*) FROM mailbox WHERE id = " + mbox.getId());
    assertEquals("Unexpected row in mailbox table", 0, results.getInt(1));

    if (TestUtil.checkLocalBlobs()) {
      // Confirm that the message directory was deleted
      assertFalse(storePath + " exists", storeDir.exists());
    }
  }

  private void cleanUp() throws Exception {
    Provisioning prov = Provisioning.getInstance();
    Account account = prov.get(AccountBy.name, TestUtil.getAddress(USER_NAME));
    if (account != null) {
      prov.deleteAccount(account.getId());
    }
  }
}

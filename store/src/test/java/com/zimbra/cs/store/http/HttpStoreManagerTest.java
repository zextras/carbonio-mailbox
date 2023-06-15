// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.http;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.FileUtil;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.db.DbMailItem;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTest;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.store.external.AbstractExternalStoreManagerTest;

public class HttpStoreManagerTest extends AbstractExternalStoreManagerTest {

    public static class MockHttpStoreManager extends HttpStoreManager {
        @Override
        protected String getGetUrl(Mailbox mbox, String locator) {
            return MockHttpStore.URL_PREFIX + locator;
        }

        @Override
        protected String getPostUrl(Mailbox mbox) {
            return MockHttpStore.URL_PREFIX;
        }

        @Override
        protected String getDeleteUrl(Mailbox mbox, String locator) {
            return MockHttpStore.URL_PREFIX + locator;
        }

        @Override
        protected String getLocator(HttpPost post, String postDigest, long postSize, Mailbox mbox, HttpResponse resp)
        throws ServiceException {
            String locator = resp.getFirstHeader("Location").getValue();
            if (locator == null || locator.isEmpty()) {
                throw ServiceException.FAILURE("no locator returned from POST", null);
            } else {
                String[] parts = locator.trim().split("/");
                return parts[parts.length - 1];
            }
        }
    }

    @Override
    public StoreManager getStoreManager() {
        return new MockHttpStoreManager();
    }

    File tmpDir;

    @BeforeEach
    public void setUpHttp() throws Exception {
        MockHttpStore.startup();
        tmpDir = Files.createTempDir();
        LC.zimbra_tmp_directory.setDefault(tmpDir.getPath());
        MailboxTestUtil.clearData();
    }

    @AfterEach
    public void tearDownHttp() throws Exception {
        MockHttpStore.shutdown();
        if (tmpDir != null) {
            FileUtil.deleteDir(tmpDir);
        }
    }

 @Test
 void mailboxDelete() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  assertEquals(0, MockHttpStore.size(), "start with no blobs in the store");

  mbox.addMessage(null, MailboxTestUtil.generateMessage("test"), MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();
  assertEquals(1, MockHttpStore.size(), "1 blob in the store");

  mbox.addMessage(null, MailboxTestUtil.generateMessage("test"), MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();
  assertEquals(2, MockHttpStore.size(), "2 blobs in the store");

  mbox.deleteMailbox();
  assertEquals(0, MockHttpStore.size(), "end with no blobs in the store");
 }

 @Test
 void fail() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  int count = countMailItems(mbox);
  MockHttpStore.setFail();
  try {
   mbox.addMessage(null, MailboxTestUtil.generateMessage("test"), MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();
   fail("expected exception not thrown");
  } catch (ServiceException expected) {

  }
  assertEquals(count, countMailItems(mbox));
 }

 @Disabled("long running test")
 @Test
 void timeout() throws Exception {
  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
  int count = countMailItems(mbox);
  MockHttpStore.setDelay();
  try {
   mbox.addMessage(null, MailboxTestUtil.generateMessage("test"), MailboxTest.STANDARD_DELIVERY_OPTIONS, null).getId();
   fail("expected exception not thrown");
  } catch (ServiceException expected) {

  }
  assertEquals(count, countMailItems(mbox));
 }


    private int countMailItems(Mailbox mbox) throws ServiceException, SQLException {
        DbConnection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = DbPool.getConnection();
            stmt = connection.prepareStatement("select count(*) from " + DbMailItem.getMailItemTableName(mbox));
            rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } finally {
            DbPool.closeResults(rs);
            DbPool.closeStatement(stmt);
            if (connection != null) {
                connection.closeQuietly();
            }
        }

    }
}

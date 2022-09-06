// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.DataSource.DataImport;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.datasource.imap.ImapSync;
import com.zimbra.cs.gal.GalImport;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.soap.admin.type.DataSourceType;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DataSourceManagerTest {
  private Account testAccount = null;
  private String OAUTH_DS_ID = "testOAuthDS";
  private String POP3_DS_ID = "testPop3DS";
  private String IMAP_DS_ID = "testImap3DS";
  private String CALDAV_DS_ID = "CalDavDS";
  private String RSS_DS_ID = "RSSDataSource";
  private String CAL_DS_ID = "CalDataSource";
  private String GAL_DS_ID = "GALDataSource";

  private String OAUTH_DS_NAME = "TestOAuthDataSource";
  private String POP3_DS_NAME = "TestPop3DataSource";
  private String IMAP_DS_NAME = "TestImapDataSource";
  private String CALDAV_DS_NAME = "TestCalDavDataSource";
  private String RSS_DS_NAME = "TestRSSDataSource";
  private String CAL_DS_NAME = "TestCalDataSource";
  private String GAL_DS_NAME = "TestGALDataSource";

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
    Provisioning prov = Provisioning.getInstance();
    testAccount = prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
  }

  @After
  public void tearDown() throws Exception {
    MailboxTestUtil.clearData();
  }

  @Test
  public void testGetDataImportWithDefaultClass() throws ServiceException {
    Map<String, Object> testAttrs = new HashMap<String, Object>();
    testAttrs.put(Provisioning.A_zimbraDataSourceDomain, "zimbra.com");
    testAttrs.put(Provisioning.A_zimbraDataSourcePort, "1234");
    testAttrs.put(Provisioning.A_zimbraDataSourceHost, "localhost");
    testAttrs.put(Provisioning.A_zimbraDataSourceUsername, "test");
    testAttrs.put(Provisioning.A_zimbraDataSourcePassword, "test");

    DataSource ds =
        new DataSource(testAccount, DataSourceType.pop3, POP3_DS_NAME, POP3_DS_ID, testAttrs, null);
    DataImport di = DataSourceManager.getInstance().getDataImport(ds);
    assertNotNull("DataImport should not be NULL", di);
    assertTrue("DataImport for 'pop3' should be Pop3Sync", di instanceof Pop3Sync);

    ds =
        new DataSource(testAccount, DataSourceType.imap, IMAP_DS_NAME, IMAP_DS_ID, testAttrs, null);
    di = DataSourceManager.getInstance().getDataImport(ds);
    assertNotNull("DataImport should not be NULL", di);
    assertTrue("DataImport for 'imap' should be ImapSync", di instanceof ImapSync);

    ds =
        new DataSource(
            testAccount, DataSourceType.caldav, CALDAV_DS_NAME, CALDAV_DS_ID, testAttrs, null);
    di = DataSourceManager.getInstance().getDataImport(ds);
    assertNotNull("DataImport should not be NULL", di);
    assertTrue(
        "DataImport for 'caldav' should be CalDavDataImport", di instanceof CalDavDataImport);

    ds = new DataSource(testAccount, DataSourceType.rss, RSS_DS_NAME, RSS_DS_ID, testAttrs, null);
    di = DataSourceManager.getInstance().getDataImport(ds);
    assertNotNull("DataImport should not be NULL", di);
    assertTrue("DataImport for 'rss' should be RssImport", di instanceof RssImport);

    ds = new DataSource(testAccount, DataSourceType.cal, CAL_DS_NAME, CAL_DS_ID, testAttrs, null);
    di = DataSourceManager.getInstance().getDataImport(ds);
    assertNotNull("DataImport should not be NULL", di);
    assertTrue("DataImport for 'cal' should be RssImport", di instanceof RssImport);

    ds = new DataSource(testAccount, DataSourceType.gal, GAL_DS_NAME, GAL_DS_ID, testAttrs, null);
    di = DataSourceManager.getInstance().getDataImport(ds);
    assertNotNull("DataImport should not be NULL", di);
    assertTrue("DataImport for 'gal' should be GalImport", di instanceof GalImport);
  }

  @Test
  public void testGetDataImportClass() throws ServiceException {
    Map<String, Object> testAttrs = new HashMap<String, Object>();
    testAttrs.put(Provisioning.A_zimbraDataSourceDomain, "zimbra.com");
    testAttrs.put(
        Provisioning.A_zimbraDataSourceImportClassName,
        "com.zimbra.cs.datasource.DataSourceManagerTest.TestDSImport");
    DataSource ds =
        new DataSource(
            testAccount, DataSourceType.unknown, OAUTH_DS_NAME, OAUTH_DS_ID, testAttrs, null);
    assertNotNull("DataSource should not be NULL", ds);
    DataImport di = DataSourceManager.getInstance().getDataImport(ds);
    assertNull("should not be able to instantiate non existent DataImport class", di);

    testAttrs.put(Provisioning.A_zimbraDataSourceImportClassName, "com.zimbra.cs.gal.GalImport");
    ds =
        new DataSource(
            testAccount, DataSourceType.unknown, OAUTH_DS_NAME, OAUTH_DS_ID, testAttrs, null);
    assertNotNull("DataSource should not be NULL", ds);
    di = DataSourceManager.getInstance().getDataImport(ds);
    assertNotNull("DataImport should not be NULL", di);
    assertTrue("DataImport for 'unknown' should be GalImport", di instanceof GalImport);
  }
}

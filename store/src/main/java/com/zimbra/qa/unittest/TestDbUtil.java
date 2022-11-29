// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.db.Db;
import com.zimbra.cs.db.DbMailbox;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.db.DbUtil;
import com.zimbra.cs.mailbox.Mailbox;

/**
 * @author bburtin
 */
public class TestDbUtil {
    private static final String USER_NAME = "TestDbUtil-user1";
    private static final Provisioning prov = Provisioning.getInstance();
    private static Server localServer = null;
    private Mailbox mbox = null;

    @BeforeClass
    public static void beforeClass() throws Exception {
        localServer = prov.getLocalServer();
    }

    @Before
    public void setUp() throws Exception {
        TestUtil.createAccount(USER_NAME);
        mbox = TestUtil.getMailbox(USER_NAME);
    }

    @Test
    public void testNormalizeSql() throws Exception {
        String sql = " \t SELECT a, 'b', 1, '', ',', NULL, '\\'' FROM table1\n\nWHERE c IN (1, 2, 3) ";
        String normalized = DbUtil.normalizeSql(sql);
        String expected = "SELECT a, XXX, XXX, XXX, XXX, XXX, XXX FROM tableXXX WHERE c IN (...)";
        assertEquals(expected, normalized);
    }

    @Test
    public void testDatabaseExists() throws Exception {
        Db db = Db.getInstance();
        String dbName = DbMailbox.getDatabaseName(mbox);
        DbConnection conn = DbPool.getConnection();

        assertTrue("Could not find database " + dbName, db.databaseExists(conn, dbName));
        assertFalse("False positive", db.databaseExists(conn, "foobar"));

        DbPool.quietClose(conn);
    }

    @After
    public void tearDown() throws Exception {
        if(TestUtil.accountExists(USER_NAME)) {
            TestUtil.deleteAccount(USER_NAME);
        }
    }
}

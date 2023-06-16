// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zimbra.common.localconfig.LC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxTestUtil;

/**
 * Unit test for {@link DbMailbox}.
 *
 * @author ysasaki
 */
public class DbMailboxTest {

    private DbConnection connection;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning.setInstance(new MockProvisioning());
        LC.zimbra_class_database.setDefault(HSQLDB.class.getName());
        DbPool.startup();
        HSQLDB.createDatabase();
    }

    @BeforeEach
    public void setUp() throws Exception {
        HSQLDB.clearDatabase();
        connection = DbPool.getConnection();
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

 @Test
 void getMailboxRawData() throws Exception {
  assertEquals(0, DbMailbox.getMailboxRawData(connection).size());

  DbMailbox.createMailbox(connection, 100, "0", "test0", 0);
  DbMailbox.createMailbox(connection, 101, "1", "test1", 0);
  DbMailbox.createMailbox(connection, 102, "2", "test2", 0);

  List<Mailbox.MailboxData> list = DbMailbox.getMailboxRawData(connection);
  assertEquals(100, list.get(0).id);
  assertEquals("0", list.get(0).accountId);
  assertEquals(101, list.get(1).id);
  assertEquals("1", list.get(1).accountId);
  assertEquals(102, list.get(2).id);
  assertEquals("2", list.get(2).accountId);
 }

 @Test
 void listAccountIds() throws Exception {
  assertEquals(0, DbMailbox.listAccountIds(connection).size());

  DbMailbox.createMailbox(connection, 100, "0", "test0", 0);
  DbMailbox.createMailbox(connection, 101, "1", "test1", 0);
  DbMailbox.createMailbox(connection, 102, "2", "test2", 0);

  Set<String> ids = DbMailbox.listAccountIds(connection);
  assertEquals(3, ids.size());
  assertTrue(ids.contains("0"));
  assertTrue(ids.contains("1"));
  assertTrue(ids.contains("2"));
 }

}

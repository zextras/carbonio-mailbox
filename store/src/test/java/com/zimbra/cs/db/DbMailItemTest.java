// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.db.DbMailItem.QueryParams;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.Flag.FlagInfo;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DbMailItem}.
 *
 * @author ysasaki
 */
public final class DbMailItemTest extends MailboxTestSuite {

  private DbConnection conn = null;
  private Mailbox mbox = null;

  @BeforeEach
  public void setUp() throws Exception {
    Account account = createAccount()
        .withAttribute(ZAttrProvisioning.A_zimbraDumpsterEnabled, "TRUE")
        .create();
    mbox = MailboxManager.getInstance().getMailboxByAccountId(account.getId());
    conn = DbPool.getConnection(mbox);
  }

  @AfterEach
  public void tearDown() {
    conn.closeQuietly();
  }
  private int insertIntoMailItem(int id, Type type, Integer indexId) throws ServiceException {
    return DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        id,
        type.toByte(),
        indexId);
  }


  private int insertIntoDumpster(int id, Type type, Integer indexId) throws ServiceException {
    return DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        id,
        type.toByte(),
        indexId);
  }


  /**
   * Checks that items with indexId == 0 (dereferred) are returned. See {@link DbMailItem#getIndexDeferredIds}
   * @throws Exception
   */
  @Test
  void getIndexDeferredIds() throws Exception {
    final Type message = Type.MESSAGE;
    final Type contact = Type.CONTACT;

    final int dereferredId = 0;
    insertIntoMailItem(100, message, dereferredId);
    insertIntoMailItem(101, message, dereferredId);
    insertIntoMailItem(102, message, dereferredId);
    insertIntoMailItem(103, message, 103);

    insertIntoMailItem(200, contact, dereferredId);
    insertIntoMailItem(201, contact, dereferredId);
    insertIntoMailItem(202, contact, 202);

    insertIntoDumpster(300, message, dereferredId);
    insertIntoDumpster(301, message, dereferredId);
    insertIntoDumpster(302, message, dereferredId);
    insertIntoDumpster(303, message, 303);
    insertIntoDumpster(400, contact, dereferredId);
    insertIntoDumpster(401, contact, dereferredId);
    insertIntoDumpster(402, contact, 402);

    Multimap<MailItem.Type, Integer> result = DbMailItem.getIndexDeferredIds(conn, mbox);

    assertEquals(10, result.size());
    assertEquals(ImmutableSet.of(100, 101, 102, 300, 301, 302), result.get(message));
    assertEquals(ImmutableSet.of(200, 201, 400, 401), result.get(contact));
  }



  @Test
  void setIndexIds() throws Exception {
    insertIntoMailItem(100, MailItem.Type.MESSAGE, 0);
    insertIntoDumpster(200, MailItem.Type.MESSAGE, 0);

    DbMailItem.setIndexIds(conn, mbox, ImmutableList.of(100, 200));
    assertEquals(
        100,
        DbUtil.executeQuery(conn, "SELECT index_id FROM mboxgroup1.mail_item WHERE id = ?", 100)
            .getInt(1));
    assertEquals(
        200,
        DbUtil.executeQuery(
                conn, "SELECT index_id FROM mboxgroup1.mail_item_dumpster WHERE id = ?", 200)
            .getInt(1));
  }

  @Test
  void getReIndexIds() throws Exception {
    insertIntoMailItem(100, MailItem.Type.MESSAGE, 0);
    insertIntoMailItem(101, MailItem.Type.MESSAGE, 0);
    insertIntoMailItem(102, MailItem.Type.MESSAGE, 0);
    insertIntoMailItem(103, MailItem.Type.MESSAGE, null);
    insertIntoMailItem(200, MailItem.Type.CONTACT, 0);
    insertIntoMailItem(201, MailItem.Type.CONTACT, 0);
    insertIntoMailItem(202, MailItem.Type.CONTACT, null);
    insertIntoDumpster(300, MailItem.Type.MESSAGE, 0);
    insertIntoDumpster(301, MailItem.Type.MESSAGE, 0);
    insertIntoDumpster(302, MailItem.Type.MESSAGE, 0);
    insertIntoDumpster(303, MailItem.Type.MESSAGE, null);
    insertIntoDumpster(400, MailItem.Type.CONTACT, 0);
    insertIntoDumpster(401, MailItem.Type.CONTACT, 0);
    insertIntoDumpster(402, MailItem.Type.CONTACT, null);

    assertEquals(
        ImmutableList.of(100, 101, 102, 300, 301, 302),
        DbMailItem.getReIndexIds(conn, mbox, EnumSet.of(MailItem.Type.MESSAGE)));
    assertEquals(
        ImmutableList.of(200, 201, 400, 401),
        DbMailItem.getReIndexIds(conn, mbox, EnumSet.of(MailItem.Type.CONTACT)));
    assertEquals(
        ImmutableList.of(100, 101, 102, 200, 201, 300, 301, 302, 400, 401),
        DbMailItem.getReIndexIds(conn, mbox, EnumSet.noneOf(MailItem.Type.class)));
  }

  @Test
  void resetIndexId() throws Exception {
    insertIntoMailItem(100, MailItem.Type.MESSAGE, 100);
    insertIntoDumpster(200, MailItem.Type.MESSAGE, 200);

    DbMailItem.resetIndexId(conn, mbox);
    assertEquals(
        0,
        DbUtil.executeQuery(conn, "SELECT index_id FROM mboxgroup1.mail_item WHERE id = ?", 100)
            .getInt(1));
    assertEquals(
        0,
        DbUtil.executeQuery(
                conn, "SELECT index_id FROM mboxgroup1.mail_item_dumpster WHERE id = ?", 200)
            .getInt(1));
  }



  @Test
  void getIds() throws Exception {
    int now = (int) (System.currentTimeMillis() / 1000);
    int beforeNow = now - 1000;
    int afterNow = now + 1000;
    int deleteNow = now + 2000;
    final int beforeNowCount = 9;
    final int afterNowCount = 13;
    final int notDeleteCount = 7;
    final int deleteCount = 17;
    int id = 100;
    Set<Integer> ids = DbMailItem.getIds(mbox, conn, new QueryParams(), false);
    QueryParams params = new QueryParams();
    params.setChangeDateBefore(now);
    Set<Integer> idsInitBeforeNow = DbMailItem.getIds(mbox, conn, params, false);
    params = new QueryParams();
    params.setChangeDateAfter(now);
    Set<Integer> idsInitAftereNow = DbMailItem.getIds(mbox, conn, params, false);

    int idsInit = ids.size();
    for (int i = 0; i < beforeNowCount; i++) {
      DbUtil.executeUpdate(
          conn,
          "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags,"
              + " tags, mod_metadata, change_date, mod_content) VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0,"
              + " ?, 0)",
          mbox.getId(),
          id++,
          MailItem.Type.MESSAGE.toByte(),
          beforeNow);
    }
    id = 200;
    Set<Integer> idsAddBeforeNow = DbMailItem.getIds(mbox, conn, new QueryParams(), false);
    assertEquals(beforeNowCount, idsAddBeforeNow.size() - idsInit);
    for (int i = 0; i < afterNowCount; i++) {
      DbUtil.executeUpdate(
          conn,
          "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags,"
              + " tags, mod_metadata, change_date, mod_content) VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0,"
              + " ?, 0)",
          mbox.getId(),
          id++,
          MailItem.Type.MESSAGE.toByte(),
          afterNow);
    }
    Set<Integer> idsAddAfterNow = DbMailItem.getIds(mbox, conn, new QueryParams(), false);
    assertEquals(afterNowCount, idsAddAfterNow.size() - idsAddBeforeNow.size());

    params = new QueryParams();
    params.setChangeDateBefore(now);
    Set<Integer> idsBeforeNow = DbMailItem.getIds(mbox, conn, params, false);
    assertEquals((idsBeforeNow.size() - idsInitBeforeNow.size()), beforeNowCount);

    params = new QueryParams();
    params.setChangeDateAfter(now);
    Set<Integer> idsAfterNow = DbMailItem.getIds(mbox, conn, params, false);
    assertEquals((idsAfterNow.size() - idsInitAftereNow.size()), afterNowCount);

    id = 300;
    for (int i = 0; i < notDeleteCount; i++) {
      DbUtil.executeUpdate(
          conn,
          "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags,"
              + " tags, mod_metadata, change_date, mod_content) VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0,"
              + " ?, 0)",
          mbox.getId(),
          id++,
          MailItem.Type.MESSAGE.toByte(),
          deleteNow);
    }
    for (int i = 0; i < deleteCount; i++) {
      DbUtil.executeUpdate(
          conn,
          "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags,"
              + " tags, mod_metadata, change_date, mod_content) VALUES(?, ?, ?, 0, 0, 0, 128, 0, 0,"
              + " ?, 0)",
          mbox.getId(),
          id++,
          MailItem.Type.MESSAGE.toByte(),
          deleteNow);
    }
    params = new QueryParams();
    params.setChangeDateAfter(deleteNow - 1);
    Set<Integer> idsForDelete = DbMailItem.getIds(mbox, conn, params, false);
    assertEquals(idsForDelete.size(), (deleteCount + notDeleteCount));

    params.setFlagToExclude(FlagInfo.DELETED);
    idsForDelete = DbMailItem.getIds(mbox, conn, params, false);
    assertEquals(idsForDelete.size(), notDeleteCount);
  }

  @Test
  void readTombstones() throws Exception {
    int now = (int) (System.currentTimeMillis() / 1000);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.tombstone "
            + "(mailbox_id, sequence, date, type, ids) "
            + "VALUES(?, ?, ?, ?, ?)",
        mbox.getId(),
        100,
        now,
        MailItem.Type.MESSAGE.toByte(),
        "1,2,3");
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.tombstone "
            + "(mailbox_id, sequence, date, type, ids) "
            + "VALUES(?, ?, ?, ?, ?)",
        mbox.getId(),
        100,
        now,
        MailItem.Type.APPOINTMENT.toByte(),
        "11,12,13,14");
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.tombstone "
            + "(mailbox_id, sequence, date, type, ids) "
            + "VALUES(?, ?, ?, ?, ?)",
        mbox.getId(),
        100,
        now,
        MailItem.Type.CONTACT.toByte(),
        "31,32");
    Set<MailItem.Type> types = new HashSet<>();
    types.add(MailItem.Type.MESSAGE);
    List<Integer> tombstones = DbMailItem.readTombstones(mbox, conn, 0, types);
    assertEquals(3, tombstones.size());
    types.add(MailItem.Type.APPOINTMENT);
    tombstones = DbMailItem.readTombstones(mbox, conn, 0, types);
    assertEquals(7, tombstones.size());

    types = new HashSet<>();
    types.add(MailItem.Type.APPOINTMENT);
    tombstones = DbMailItem.readTombstones(mbox, conn, 0, types);
    assertEquals(4, tombstones.size());

    types = new HashSet<>();
    types.add(MailItem.Type.CONTACT);
    tombstones = DbMailItem.readTombstones(mbox, conn, 0, types);
    assertEquals(2, tombstones.size());

    types = new HashSet<>();
    types.add(MailItem.Type.MESSAGE);
    types.add(MailItem.Type.APPOINTMENT);
    tombstones = DbMailItem.readTombstones(mbox, conn, 0, types);
    assertEquals(7, tombstones.size());
  }
}

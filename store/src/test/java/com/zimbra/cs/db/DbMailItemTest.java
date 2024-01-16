// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbMailItem.QueryParams;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.Flag.FlagInfo;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DbMailItem}.
 *
 * @author ysasaki
 */
public final class DbMailItemTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();
    final String accountName = UUID.randomUUID() + "@"+ UUID.randomUUID() +".com";
    prov.createAccount(accountName, "secret", new HashMap<>());
  }

  private DbConnection conn = null;
  private Mailbox mbox = null;

  @BeforeEach
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
    mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
    conn = DbPool.getConnection(mbox);
  }

  @AfterEach
  public void tearDown() {
    conn.closeQuietly();
  }

  @Test
  void getIndexDeferredIds() throws Exception {
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        100,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        101,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        102,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        103,
        MailItem.Type.MESSAGE.toByte(),
        103);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        200,
        MailItem.Type.CONTACT.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        201,
        MailItem.Type.CONTACT.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        202,
        MailItem.Type.CONTACT.toByte(),
        202);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        300,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        301,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        302,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        303,
        MailItem.Type.MESSAGE.toByte(),
        303);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        400,
        MailItem.Type.CONTACT.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        401,
        MailItem.Type.CONTACT.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        402,
        MailItem.Type.CONTACT.toByte(),
        402);

    Multimap<MailItem.Type, Integer> result = DbMailItem.getIndexDeferredIds(conn, mbox);
    assertEquals(10, result.size());
    assertEquals(ImmutableSet.of(100, 101, 102, 300, 301, 302), result.get(MailItem.Type.MESSAGE));
    assertEquals(ImmutableSet.of(200, 201, 400, 401), result.get(MailItem.Type.CONTACT));
  }

  @Test
  void setIndexIds() throws Exception {
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        100,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        200,
        MailItem.Type.MESSAGE.toByte(),
        0);

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
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        100,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        101,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        102,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        103,
        MailItem.Type.MESSAGE.toByte(),
        null);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        200,
        MailItem.Type.CONTACT.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        201,
        MailItem.Type.CONTACT.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        202,
        MailItem.Type.CONTACT.toByte(),
        null);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        300,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        301,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        302,
        MailItem.Type.MESSAGE.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        303,
        MailItem.Type.MESSAGE.toByte(),
        null);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        400,
        MailItem.Type.CONTACT.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        401,
        MailItem.Type.CONTACT.toByte(),
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        402,
        MailItem.Type.CONTACT.toByte(),
        null);

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
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        100,
        MailItem.Type.MESSAGE.toByte(),
        100);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item_dumpster (mailbox_id, id, type, index_id, date, size,"
            + " flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        200,
        MailItem.Type.MESSAGE.toByte(),
        200);

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
  void completeConversation() throws Exception {
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        200,
        MailItem.Type.CONVERSATION.toByte());
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, index_id, date, size, flags, tags,"
            + " mod_metadata, mod_content) VALUES(?, ?, ?, 0, 0, 0, 0, 0, 0, 0)",
        mbox.getId(),
        201,
        MailItem.Type.CONVERSATION.toByte());

    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, folder_id, parent_id, index_id,"
            + " date, size, flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, ?, 0, 0, 0,"
            + " ?, 0, 0, 0)",
        mbox.getId(),
        100,
        MailItem.Type.MESSAGE.toByte(),
        Mailbox.ID_FOLDER_INBOX,
        200,
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, folder_id, parent_id, index_id,"
            + " date, size, flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, ?, 0, 0, 0,"
            + " ?, 0, 0, 0)",
        mbox.getId(),
        101,
        MailItem.Type.MESSAGE.toByte(),
        Mailbox.ID_FOLDER_INBOX,
        200,
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, folder_id, parent_id, index_id,"
            + " date, size, flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, ?, 0, 0, 0,"
            + " ?, 0, 0, 0)",
        mbox.getId(),
        102,
        MailItem.Type.MESSAGE.toByte(),
        Mailbox.ID_FOLDER_INBOX,
        200,
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, folder_id, parent_id, index_id,"
            + " date, size, flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, ?, 0, 0, 0,"
            + " ?, 0, 0, 0)",
        mbox.getId(),
        103,
        MailItem.Type.MESSAGE.toByte(),
        Mailbox.ID_FOLDER_INBOX,
        201,
        Flag.BITMASK_FROM_ME);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, folder_id, parent_id, index_id,"
            + " date, size, flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, ?, 0, 0, 0,"
            + " ?, 0, 0, 0)",
        mbox.getId(),
        104,
        MailItem.Type.MESSAGE.toByte(),
        Mailbox.ID_FOLDER_INBOX,
        201,
        0);
    DbUtil.executeUpdate(
        conn,
        "INSERT INTO mboxgroup1.mail_item (mailbox_id, id, type, folder_id, parent_id, index_id,"
            + " date, size, flags, tags, mod_metadata, mod_content) VALUES(?, ?, ?, ?, ?, 0, 0, 0,"
            + " ?, 0, 0, 0)",
        mbox.getId(),
        105,
        MailItem.Type.MESSAGE.toByte(),
        Mailbox.ID_FOLDER_INBOX,
        201,
        0);

    MailItem.UnderlyingData data = new MailItem.UnderlyingData();
    data.id = 200;
    data.type = MailItem.Type.CONVERSATION.toByte();
    DbMailItem.completeConversation(mbox, conn, data);
    assertFalse(data.isSet(Flag.FlagInfo.FROM_ME));

    data = new MailItem.UnderlyingData();
    data.id = 201;
    data.type = MailItem.Type.CONVERSATION.toByte();
    DbMailItem.completeConversation(mbox, conn, data);
    assertTrue(data.isSet(Flag.FlagInfo.FROM_ME));
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

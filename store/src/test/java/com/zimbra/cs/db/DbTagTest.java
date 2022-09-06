package com.zimbra.cs.db;

import static com.zimbra.cs.db.DbTag.TAG_FIELDS;
import static com.zimbra.cs.db.DbTag.asUnderlyingData;
import static com.zimbra.cs.db.DbTag.deserializeTags;
import static com.zimbra.cs.db.DbTag.getTagTableName;
import static com.zimbra.cs.db.DbTag.getTaggedItemTableName;
import static com.zimbra.cs.db.DbTag.inThisMailboxAnd;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;
import com.zimbra.cs.mailbox.Mailbox;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;

public class DbTagTest {

  private static void verifyTagCounts(
      DbConnection conn, Mailbox mbox, Map<Integer, UnderlyingData> tdata) throws ServiceException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      String mailboxesMatchAnd =
          DebugConfig.disableMailboxGroups ? "" : "ti.mailbox_id = mi.mailbox_id AND ";
      stmt =
          conn.prepareStatement(
              "SELECT ti.tag_id, COUNT(ti.item_id), "
                  + Db.clauseIFNULL("SUM(mi.unread)", "0")
                  + " FROM "
                  + getTaggedItemTableName(mbox, "ti")
                  + " INNER JOIN "
                  + DbMailItem.getMailItemTableName(mbox, "mi")
                  + " ON "
                  + mailboxesMatchAnd
                  + "mi.id = ti.item_id"
                  + " WHERE "
                  + inThisMailboxAnd("ti")
                  + "ti.tag_id > 0 AND "
                  + Db.getInstance().bitAND("mi.flags", String.valueOf(Flag.BITMASK_DELETED))
                  + " = 0"
                  + " GROUP BY ti.tag_id");
      DbMailItem.setMailboxId(stmt, mbox, 1);

      rs = stmt.executeQuery();
      Map<Integer, UnderlyingData> tcheck = new HashMap<Integer, UnderlyingData>(tdata);
      while (rs.next()) {
        int id = rs.getInt(1), size = rs.getInt(2), unread = rs.getInt(3);
        UnderlyingData data = tcheck.remove(id);
        Assert.assertNotNull("no TAG row for id " + id, data);
        Assert.assertEquals("size for tag " + data.name, size, data.size);
        Assert.assertEquals("unread for tag " + data.name, unread, data.unreadCount);
      }
      for (UnderlyingData data : tcheck.values()) {
        Assert.assertEquals("size for tag " + data.name, 0, data.size);
        Assert.assertEquals("unread for tag " + data.name, 0, data.unreadCount);
      }
    } catch (SQLException e) {
      throw ServiceException.FAILURE("consistency checking TAGGED_ITEM vs. TAG", e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
    }
  }

  public static <T> void assertCollectionsEqual(
      String msg, java.util.Collection<T> expected, java.util.Collection<T> actual) {
    String prefix = msg == null ? "" : msg + ": ";
    if (expected == null) {
      Assert.assertNull(prefix + "expected <null>", actual);
    } else if (actual == null) {
      Assert.fail(prefix + "was <null>, expected " + expected);
    } else {
      Assert.assertEquals(prefix + "collection size", expected.size(), actual.size());
      for (T t : expected) {
        if (!actual.contains(t)) {
          Assert.fail(prefix + "actual collection does not contain <" + t + ">");
        }
      }
    }
  }

  private static void verifyTaggedItem(
      DbConnection conn, Mailbox mbox, Map<Integer, UnderlyingData> tdata) throws ServiceException {
    int flagMask = 0;
    for (int flagId : Mailbox.REIFIED_FLAGS) {
      flagMask |= 1 << (-flagId - 1);
    }

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt =
          conn.prepareStatement(
              "SELECT id, flags, tag_names, unread FROM "
                  + DbMailItem.getMailItemTableName(mbox)
                  + " WHERE "
                  + DbMailItem.IN_THIS_MAILBOX_AND
                  + "type NOT IN "
                  + DbMailItem.NON_SEARCHABLE_TYPES);
      DbMailItem.setMailboxId(stmt, mbox, 1);

      rs = stmt.executeQuery();
      while (rs.next()) {
        int id = rs.getInt(1);
        int flags = (rs.getInt(2) & flagMask) | (rs.getBoolean(4) ? Flag.BITMASK_UNREAD : 0);
        String[] tagset = deserializeTags(rs.getString(3));
        Set<String> tags =
            tagset == null ? Collections.<String>emptySet() : Sets.newHashSet(tagset);

        PreparedStatement stmtcheck = null;
        ResultSet rscheck = null;
        try {
          // make sure the item counts match the tag totals
          String mailboxesMatchAnd =
              DebugConfig.disableMailboxGroups ? "" : "tag.mailbox_id = ti.mailbox_id AND ";
          stmtcheck =
              conn.prepareStatement(
                  "SELECT id, name FROM "
                      + getTagTableName(mbox, "tag")
                      + " INNER JOIN "
                      + getTaggedItemTableName(mbox, "ti")
                      + " ON "
                      + mailboxesMatchAnd
                      + "tag.id = ti.tag_id"
                      + " WHERE "
                      + inThisMailboxAnd("ti")
                      + "ti.item_id = ?");
          int pos = 1;
          pos = DbMailItem.setMailboxId(stmtcheck, mbox, pos);
          stmtcheck.setInt(pos++, id);

          rscheck = stmtcheck.executeQuery();
          int flagcheck = 0;
          Set<String> tagcheck = Sets.newHashSetWithExpectedSize(tags.size());
          while (rscheck.next()) {
            int idcheck = rscheck.getInt(1);
            String namecheck = rscheck.getString(2);
            if (idcheck < 0) {
              flagcheck |= 1 << (-idcheck - 1);
            } else {
              tagcheck.add(namecheck);
            }
          }
          Assert.assertEquals("flags for item " + id, flags, flagcheck);
          Assert.assertEquals("tags for item " + id, tags, tagcheck);
        } catch (SQLException e) {
          throw ServiceException.FAILURE("consistency checking TAGGED_ITEM vs. MAIL_ITEM", e);
        } finally {
          DbPool.closeResults(rscheck);
          DbPool.closeStatement(stmtcheck);
        }
      }
    } catch (SQLException e) {
      throw ServiceException.FAILURE("consistency checking TAGGED_ITEM vs. MAIL_ITEM", e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
    }
  }

  private static void validateTaggedItem(
      DbConnection conn, Mailbox mbox, Map<Integer, UnderlyingData> tdata) throws ServiceException {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      String mailboxesMatchAnd =
          DebugConfig.disableMailboxGroups ? "" : "ti.mailbox_id = mi.mailbox_id AND ";
      stmt =
          conn.prepareStatement(
              "SELECT mi.id, mi.type, ti.tag_id "
                  + " FROM "
                  + getTaggedItemTableName(mbox, "ti")
                  + " INNER JOIN "
                  + DbMailItem.getMailItemTableName(mbox, "mi")
                  + " ON "
                  + mailboxesMatchAnd
                  + "mi.id = ti.item_id"
                  + " WHERE "
                  + DbMailItem.IN_THIS_MAILBOX_AND
                  + "type IN "
                  + DbMailItem.NON_SEARCHABLE_TYPES);
      DbMailItem.setMailboxId(stmt, mbox, 1);

      rs = stmt.executeQuery();
      while (rs.next()) {
        int itemId = rs.getInt(1), type = rs.getInt(2), tagId = rs.getInt(3);
        String tname = tagId > 0 ? tdata.get(tagId).name : mbox.getFlagById(tagId).getName();
        Assert.fail("found tag " + tname + " on " + MailItem.Type.of((byte) type) + " " + itemId);
      }
    } catch (SQLException e) {
      throw ServiceException.FAILURE("validating TAGGED_ITEM entries", e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
    }
  }

  public static void debugConsistencyCheck(Mailbox mbox) throws ServiceException {
    DbConnection conn =
        mbox.lock.isWriteLockedByCurrentThread()
            ? mbox.getOperationConnection()
            : DbPool.getConnection(mbox);

    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      Map<Integer, UnderlyingData> tdata = Maps.newHashMap();
      // make sure the item counts match the tag totals
      stmt =
          conn.prepareStatement(
              "SELECT "
                  + TAG_FIELDS
                  + " FROM "
                  + getTagTableName(mbox)
                  + " WHERE "
                  + DbMailItem.IN_THIS_MAILBOX_AND
                  + "id > 0");
      DbMailItem.setMailboxId(stmt, mbox, 1);

      rs = stmt.executeQuery();
      while (rs.next()) {
        UnderlyingData data = asUnderlyingData(rs);
        tdata.put(data.id, data);
      }
      DbPool.closeResults(rs);
      rs = null;
      DbPool.closeStatement(stmt);
      stmt = null;

      // catch spurious TAGGED_ITEM entries
      validateTaggedItem(conn, mbox, tdata);
      // make sure the TAGGED_ITEM table is accurate
      verifyTaggedItem(conn, mbox, tdata);
      // make sure the item counts match the tag totals
      verifyTagCounts(conn, mbox, tdata);
    } catch (SQLException e) {
      throw ServiceException.FAILURE(
          "consistency checking tags/flags for mailbox " + mbox.getId(), e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
      if (!mbox.lock.isWriteLockedByCurrentThread()) {
        conn.close();
      }
    }
  }

  //    private static void verifyTagCounts(DbConnection conn, Mailbox mbox, Map<Integer,
  // UnderlyingData> tdata) throws ServiceException {
  //        PreparedStatement stmt = null;
  //        ResultSet rs = null;
  //        try {
  //            String mailboxesMatchAnd = DebugConfig.disableMailboxGroups ? "" : "ti.mailbox_id =
  // mi.mailbox_id AND ";
  //            stmt = conn.prepareStatement("SELECT ti.tag_id, mi.id, mi.unread, mi.flags, mi.type"
  // +
  //                    " FROM " + getTaggedItemTableName(mbox, "ti") +
  //                    " INNER JOIN " + DbMailItem.getMailItemTableName(mbox, "mi") + " ON " +
  // mailboxesMatchAnd + "mi.id = ti.item_id" +
  //                    " WHERE " + inThisMailboxAnd("ti") + "ti.tag_id > 0 AND " +
  // Db.getInstance().bitAND("mi.flags", String.valueOf(Flag.BITMASK_DELETED)) + " = 0");
  ////                    " WHERE " + inThisMailboxAnd("ti") + "ti.tag_id > 0");
  //            DbMailItem.setMailboxId(stmt, mbox, 1);
  //            rs = stmt.executeQuery();
  //
  //            while (rs.next()) {
  //                int tagId = rs.getInt(1), itemId = rs.getInt(2), unread = rs.getInt(3), flags =
  // rs.getInt(4), type = rs.getInt(5);
  //                System.out.println(MailItem.Type.of((byte) type) + " " + itemId + ": tag " +
  // tdata.get(tagId).name + ": unread " + (unread > 0) +
  //                        ", deleted " + ((flags & Flag.BITMASK_DELETED) != 0) + " (flags " +
  // flags + ")");
  //            }
  //            System.out.println("-----------------------");
  //        } catch (SQLException e) {
  //            throw ServiceException.FAILURE("consistency checking TAGGED_ITEM vs. TAG", e);
  //        } finally {
  //            DbPool.closeResults(rs);
  //            DbPool.closeStatement(stmt);
  //        }
  //    }
}

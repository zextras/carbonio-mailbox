// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import static com.zimbra.cs.db.DbMailItem.constructItem;
import static com.zimbra.cs.db.DbMailItem.DB_FIELDS;
import static com.zimbra.cs.db.DbMailItem.getCalendarItemTableName;
import static com.zimbra.cs.db.DbMailItem.getMailItemTableName;
import static com.zimbra.cs.db.DbMailItem.setMailboxId;
import static com.zimbra.cs.db.DbMailItem.IN_THIS_MAILBOX_AND;
import static com.zimbra.cs.db.DbMailItem.MAILBOX_ID;
import static com.zimbra.cs.db.DbMailItem.MAILBOX_ID_VALUE;
import static com.zimbra.cs.db.DbMailItem.typeIn;
import static com.zimbra.cs.db.DbMailItem.withMailboxId;

import com.zextras.mailbox.calendar.CalendarItemRepository;
import com.zextras.db.DbError;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Metadata;
import com.zimbra.cs.mailbox.util.TypedIdList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class DbCalendarItem implements CalendarItemRepository {

  private static final String CALENDAR_TYPES = "(" + MailItem.Type.APPOINTMENT.toByte() + ')';

  @Override
  public void copyCalendarItem(CalendarItem calItem, int newId, boolean fromDumpster)
      throws ServiceException {
    Mailbox mbox = calItem.getMailbox();
    if (newId <= 0) {
      throw ServiceException.FAILURE("invalid data for DB item copy", null);
    }

    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    try {
      stmt =
          conn.prepareStatement(
              "INSERT INTO "
                  + getCalendarItemTableName(mbox)
                  + "("
                  + MAILBOX_ID
                  + " uid, item_id, start_time, end_time) "
                  + "SELECT "
                  + MAILBOX_ID_VALUE
                  + " uid, ?, start_time, end_time FROM "
                  + getCalendarItemTableName(mbox, fromDumpster)
                  + " WHERE "
                  + IN_THIS_MAILBOX_AND
                  + "item_id = ?");
      int pos = 1;
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setInt(pos++, newId);
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setInt(pos++, calItem.getId());
      int num = stmt.executeUpdate();
      if (num != 1) {
        throw ServiceException.FAILURE("failed to create object", null);
      }
    } catch (SQLException e) {
      if (conn.getDb().matchesError(e, DbError.DUPLICATE_ROW)) {
        throw MailServiceException.ALREADY_EXISTS(newId, e);
      } else {
        throw ServiceException.FAILURE("copying " + calItem.getType() + ": " + calItem.getId(), e);
      }
    } finally {
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public UnderlyingData getCalendarItem(Mailbox mbox, String uid) throws ServiceException {
    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt =
          conn.prepareStatement(
              "SELECT "
                  + DB_FIELDS
                  + " FROM "
                  + getCalendarItemTableName(mbox, "ci")
                  + ", "
                  + getMailItemTableName(mbox, "mi")
                  + " WHERE ci.uid = ? AND mi.id = ci.item_id AND mi.type IN "
                  + CALENDAR_TYPES
                  + " AND ci.mailbox_id = ? AND mi.mailbox_id = ci.mailbox_id");

      int pos = 1;
      stmt.setString(pos++, uid);
      pos = setMailboxId(stmt, mbox, pos);
      rs = stmt.executeQuery();

      if (rs.next()) {
        return constructItem(rs);
      }
      return null;
    } catch (SQLException e) {
      throw ServiceException.FAILURE("fetching calendar items for mailbox " + mbox.getId(), e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public List<UnderlyingData> getCalendarItems(
      Mailbox mbox, MailItem.Type type, long start, long end, int folderId, int[] excludeFolderIds)
      throws ServiceException {
    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt =
          calendarItemStatement(
              conn, DB_FIELDS, mbox, type, start, end, folderId, excludeFolderIds);
      rs = stmt.executeQuery();

      List<UnderlyingData> result = new ArrayList<>();
      while (rs.next()) {
        result.add(constructItem(rs));
      }
      return result;
    } catch (SQLException e) {
      throw ServiceException.FAILURE("fetching calendar items for mailbox " + mbox.getId(), e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public List<UnderlyingData> getCalendarItems(Mailbox mbox, List<String> uids)
      throws ServiceException {
    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<UnderlyingData> result = new ArrayList<>();
    try {
      for (int i = 0; i < uids.size(); i += DbUtil.IN_CLAUSE_BATCH_SIZE) {
        int count = Math.min(DbUtil.IN_CLAUSE_BATCH_SIZE, uids.size() - i);
        stmt =
            conn.prepareStatement(
                "SELECT "
                    + DB_FIELDS
                    + " FROM "
                    + getCalendarItemTableName(mbox, "ci")
                    + ", "
                    + getMailItemTableName(mbox, "mi")
                    + " WHERE mi.id = ci.item_id AND mi.type IN "
                    + CALENDAR_TYPES
                    + " AND ci.mailbox_id = ? AND mi.mailbox_id = ci.mailbox_id"
                    + " AND "
                    + DbUtil.whereIn("ci.uid", count));
        int pos = 1;
        pos = setMailboxId(stmt, mbox, pos);
        for (int index = i; index < i + count; index++) {
          stmt.setString(pos++, uids.get(index));
        }
        rs = stmt.executeQuery();
        while (rs.next()) {
          result.add(constructItem(rs));
        }
        stmt.close();
        stmt = null;
      }
      return result;
    } catch (SQLException e) {
      throw ServiceException.FAILURE("fetching calendar items for mailbox " + mbox.getId(), e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public TypedIdList listCalendarItems(
      Mailbox mbox, MailItem.Type type, long start, long end, int folderId, int[] excludeFolderIds)
      throws ServiceException {
    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt =
          calendarItemStatement(
              conn, "mi.id, mi.type, mi.uuid", mbox, type, start, end, folderId, excludeFolderIds);
      rs = stmt.executeQuery();

      TypedIdList result = new TypedIdList();
      while (rs.next()) {
        result.add(MailItem.Type.of(rs.getByte(2)), rs.getInt(1), rs.getString(3));
      }
      return result;
    } catch (SQLException e) {
      throw ServiceException.FAILURE("listing calendar items for mailbox " + mbox.getId(), e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
    }
  }

  private PreparedStatement calendarItemStatement(
      DbConnection conn,
      String fields,
      Mailbox mbox,
      MailItem.Type type,
      long start,
      long end,
      int folderId,
      int[] excludeFolderIds)
      throws SQLException {
    boolean folderSpecified = folderId != Mailbox.ID_AUTO_INCREMENT;

    String endConstraint = (end != -1) ? " AND ci.start_time < ?" : "";
    String startConstraint = (start != -1) ? " AND ci.end_time > ?" : "";
    String typeConstraint =
        type == MailItem.Type.UNKNOWN ? "type IN " + CALENDAR_TYPES : typeIn(type);

    String excludeFolderPart = "";
    if (excludeFolderIds != null && excludeFolderIds.length > 0) {
      excludeFolderPart = " AND " + DbUtil.whereNotIn("folder_id", excludeFolderIds.length);
    }

    PreparedStatement stmt =
        conn.prepareStatement(
            "SELECT "
                + fields
                + " FROM "
                + getCalendarItemTableName(mbox, "ci")
                + ", "
                + getMailItemTableName(mbox, "mi")
                + " WHERE mi.id = ci.item_id"
                + endConstraint
                + startConstraint
                + " AND mi."
                + typeConstraint
                + " AND ci.mailbox_id = ? AND mi.mailbox_id = ci.mailbox_id"
                + (folderSpecified ? " AND folder_id = ?" : "")
                + excludeFolderPart);

    int pos = 1;
    if (!endConstraint.isEmpty()) {
      stmt.setTimestamp(pos++, new Timestamp(end));
    }
    if (!startConstraint.isEmpty()) {
      stmt.setTimestamp(pos++, new Timestamp(start));
    }
    pos = setMailboxId(stmt, mbox, pos);
    if (folderSpecified) {
      stmt.setInt(pos++, folderId);
    }
    if (excludeFolderIds != null) {
      for (int id : excludeFolderIds) {
        stmt.setInt(pos++, id);
      }
    }

    return stmt;
  }

  @Override
  public void addCalendar(CalendarItem calItem) throws ServiceException {
    Mailbox mbox = calItem.getMailbox();

    long start = calItem.getStartTime();
    long end = calItem.getEndTime();
    Timestamp startTs = new Timestamp(start);
    Timestamp endTs = getEnd(start, end);

    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    try {
      stmt =
          conn.prepareStatement(
              "INSERT INTO "
                  + getCalendarItemTableName(mbox)
                  + " ("
                  + MAILBOX_ID
                  + "uid, item_id, start_time, end_time)"
                  + " VALUES ("
                  + "?, "
                  + "?, ?, ?, ?)");
      int pos = 1;
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setString(pos++, calItem.getUid());
      stmt.setInt(pos++, calItem.getId());
      stmt.setTimestamp(pos++, startTs);
      stmt.setTimestamp(pos++, endTs);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw ServiceException.FAILURE(
          "writing invite to calendar item table: UID=" + calItem.getUid(), e);
    } finally {
      DbPool.closeStatement(stmt);
    }
  }

  private static long MAX_DATE = new GregorianCalendar(9999, 1, 1).getTimeInMillis();

  private static Timestamp getEnd(long start, long end) {
    if (end <= 0) {
      if ((end == 0)
          || (end == -1)
          || (end < start)
          || (end > start + Constants.MILLIS_PER_DAY * 366)) {
        end = MAX_DATE;
      }
    }
    return new Timestamp(end);
  }

  @Override
  public void updateCalendar(CalendarItem calItem) throws ServiceException {
    Mailbox mbox = calItem.getMailbox();
    long start = calItem.getStartTime();
    long end = calItem.getEndTime();
    Timestamp startTs = new Timestamp(start);
    Timestamp endTs = getEnd(start, end);

    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    try {
      stmt =
          conn.prepareStatement(
              DbUtil.upsert(
                      getCalendarItemTableName(mbox),
                      withMailboxId("uid", "item_id", "start_time", "end_time"),
                      withMailboxId("uid"),
                      List.of("uid", "item_id", "start_time", "end_time")));
      int pos = 1;
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setString(pos++, calItem.getUid());
      stmt.setInt(pos++, calItem.getId());
      stmt.setTimestamp(pos++, startTs);
      stmt.setTimestamp(pos++, endTs);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw ServiceException.FAILURE(
          "writing invite to calendar item table " + calItem.getUid(), e);
    } finally {
      DbPool.closeStatement(stmt);
    }
  }


  @Override
  public List<CalendarItem.CalendarMetadata> getCalendarItemMetadata(
      Folder folder, long start, long end) throws ServiceException {
    Mailbox mbox = folder.getMailbox();

    ArrayList<CalendarItem.CalendarMetadata> result =
        new ArrayList<>();

    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      String startConstraint = (start != -1) ? " AND ci.end_time > ?" : "";
      String endConstraint = (end != -1) ? " AND ci.start_time < ?" : "";
      String folderConstraint = " AND mi.folder_id = ?";
      stmt =
          conn.prepareStatement(
              "SELECT mi.mailbox_id, mi.id, ci.uid, mi.mod_metadata, mi.mod_content, ci.start_time,"
                  + " ci.end_time FROM "
                  + getMailItemTableName(mbox, "mi")
                  + ", "
                  + getCalendarItemTableName(mbox, "ci")
                  + " WHERE mi.mailbox_id = ci.mailbox_id AND mi.id = ci.item_id"
                  + " AND mi.mailbox_id = ? "
                  + startConstraint
                  + endConstraint
                  + folderConstraint);
      int pos = 1;
      pos = setMailboxId(stmt, mbox, pos);
      if (!startConstraint.isEmpty()) {
        stmt.setTimestamp(pos++, new Timestamp(start));
      }
      if (!endConstraint.isEmpty()) {
        stmt.setTimestamp(pos++, new Timestamp(end));
      }
      stmt.setInt(pos++, folder.getId());
      rs = stmt.executeQuery();
      while (rs.next()) {
        result.add(
            new CalendarItem.CalendarMetadata(
                rs.getInt(1),
                rs.getInt(2),
                rs.getString(3),
                rs.getInt(4),
                rs.getInt(5),
                rs.getTimestamp(6).getTime(),
                rs.getTimestamp(7).getTime()));
      }
    } catch (SQLException e) {
      throw ServiceException.FAILURE("fetching CalendarItem Metadata for mbox " + mbox.getId(), e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
    }
    return result;
  }
}

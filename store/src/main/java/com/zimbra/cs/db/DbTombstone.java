// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import static com.zimbra.cs.db.DbMailItem.getTombstoneTableName;
import static com.zimbra.cs.db.DbMailItem.setMailboxId;
import static com.zimbra.cs.db.DbMailItem.IN_THIS_MAILBOX_AND;
import static com.zimbra.cs.db.DbMailItem.MAILBOX_ID;
import static com.zimbra.cs.db.DbMailItem.MAILBOX_ID_VALUE;
import static com.zimbra.cs.db.DbMailItem.MAX_TEXT_LENGTH;

import com.zextras.mailbox.mail.TombstoneRepository;
import com.google.common.base.Strings;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.util.TypedIdList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DbTombstone implements TombstoneRepository {

  @Override
  public void writeTombstones(Mailbox mbox, TypedIdList tombstones) throws ServiceException {
    if (tombstones == null || tombstones.isEmpty()) {
      return;
    }

    for (Map.Entry<MailItem.Type, List<TypedIdList.ItemInfo>> entry : tombstones) {
      MailItem.Type type = entry.getKey();
      switch (type) {
        case VIRTUAL_CONVERSATION:
          continue;
      }

      StringBuilder row = new StringBuilder();
      for (TypedIdList.ItemInfo stone : entry.getValue()) {
        row.append(row.length() == 0 ? "" : ",").append(stone.getId());
        if (stone.getUuid() != null) {
          row.append(':').append(stone.getUuid());
        }

        if (row.length() > MAX_TEXT_LENGTH - 128) {
          writeTombstone(mbox, type, row.toString());
          row.setLength(0);
        }
      }

      writeTombstone(mbox, type, row.toString());
    }
  }

  private void writeTombstone(Mailbox mbox, MailItem.Type type, String row)
      throws ServiceException {
    if (Strings.isNullOrEmpty(row)) {
      return;
    }

    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    try {
      stmt =
          conn.prepareStatement(
              "INSERT INTO "
                  + getTombstoneTableName(mbox)
                  + "("
                  + MAILBOX_ID
                  + "sequence, date, type, ids)"
                  + " VALUES ("
                  + MAILBOX_ID_VALUE
                  + "?, ?, ?, ?)");
      int pos = 1;
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setInt(pos++, mbox.getOperationChangeID());
      stmt.setInt(pos++, mbox.getOperationTimestamp());
      stmt.setByte(pos++, type.toByte());
      stmt.setString(pos++, row);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw ServiceException.FAILURE("writing tombstones for " + type + "(s): " + row, e);
    } finally {
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public TypedIdList readTombstones(Mailbox mbox, long lastSync, boolean equalModSeq)
      throws ServiceException {
    TypedIdList tombstones = new TypedIdList();

    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      if (equalModSeq) {
        stmt =
            conn.prepareStatement(
                "SELECT type, ids, sequence FROM "
                    + getTombstoneTableName(mbox)
                    + " WHERE "
                    + IN_THIS_MAILBOX_AND
                    + "sequence >= ? AND ids IS NOT NULL"
                    + " ORDER BY sequence");
      } else {
        stmt =
            conn.prepareStatement(
                "SELECT type, ids, sequence FROM "
                    + getTombstoneTableName(mbox)
                    + " WHERE "
                    + IN_THIS_MAILBOX_AND
                    + "sequence > ? AND ids IS NOT NULL"
                    + " ORDER BY sequence");
      }

      DbUtil.enableStreaming(stmt);
      int pos = 1;
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setLong(pos++, lastSync);
      rs = stmt.executeQuery();

      while (rs.next()) {
        MailItem.Type type = MailItem.Type.of(rs.getByte(1));
        String row = rs.getString(2);
        int modSeq = rs.getInt(3);
        if (row == null || row.equals("")) {
          continue;
        }

        for (String stone : row.split(",")) {
          try {
            int delimiter = stone.indexOf(':');
            if (delimiter == -1) {
              tombstones.add(type, Integer.parseInt(stone), null, modSeq);
            } else {
              tombstones.add(
                  type,
                  Integer.parseInt(stone.substring(0, delimiter)),
                  Strings.emptyToNull(stone.substring(delimiter + 1)),
                  modSeq);
            }
          } catch (NumberFormatException nfe) {
            ZimbraLog.sync.warn("unparseable TOMBSTONE entry: " + stone);
          }
        }
      }
      return tombstones;
    } catch (SQLException e) {
      throw ServiceException.FAILURE("reading tombstones since change: " + lastSync, e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public int purgeTombstones(Mailbox mbox, int beforeDate) throws ServiceException {
    int cutoff = 0;

    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt =
          conn.prepareStatement(
              "SELECT MAX(sequence) FROM "
                  + getTombstoneTableName(mbox)
                  + " WHERE "
                  + IN_THIS_MAILBOX_AND
                  + "date <= ?");
      int pos = 1;
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setLong(pos++, beforeDate);
      rs = stmt.executeQuery();
      if (rs.next()) {
        cutoff = rs.getInt(1);
      }

      if (cutoff > 0) {
        stmt =
            conn.prepareStatement(
                "DELETE FROM "
                    + getTombstoneTableName(mbox)
                    + " WHERE "
                    + IN_THIS_MAILBOX_AND
                    + "sequence <= ?");
        pos = 1;
        pos = setMailboxId(stmt, mbox, pos);
        stmt.setLong(pos++, cutoff);
        int numRows = stmt.executeUpdate();
        if (numRows > 0) {
          ZimbraLog.mailbox.info("Purged %d tombstones dated before %d.", numRows, beforeDate);
        }
      }

      return cutoff;
    } catch (SQLException e) {
      throw ServiceException.FAILURE("purging tombstones with date before " + beforeDate, e);
    } finally {
      DbPool.closeStatement(stmt);
    }
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import static com.zimbra.cs.db.DbMailItem.constructItem;
import static com.zimbra.cs.db.DbMailItem.DB_FIELDS;
import static com.zimbra.cs.db.DbMailItem.getConversationTableName;
import static com.zimbra.cs.db.DbMailItem.getMailItemTableName;
import static com.zimbra.cs.db.DbMailItem.setMailboxId;
import static com.zimbra.cs.db.DbMailItem.IN_THIS_MAILBOX_AND;
import static com.zimbra.cs.db.DbMailItem.withMailboxId;
import static com.zimbra.cs.db.DbMailItem.completeConversation;
import static com.zimbra.cs.db.DbMailItem.CI_ID;

import com.zextras.mailbox.mail.ConversationRepository;
import com.google.common.collect.Sets;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ListUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.VirtualConversation;
import com.zimbra.cs.mailbox.util.TypedIdList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DbConversation implements ConversationRepository {

  @Override
  public void reparentChildren(MailItem oldParent, MailItem newParent)
      throws ServiceException {
    if (oldParent == newParent) {
      return;
    }
    Mailbox mbox = oldParent.getMailbox();
    if (mbox != newParent.getMailbox()) {
      throw MailServiceException.WRONG_MAILBOX();
    }
    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    try {
      String relation = (oldParent instanceof VirtualConversation ? "id = ?" : "parent_id = ?");

      stmt =
          conn.prepareStatement(
              "UPDATE "
                  + getMailItemTableName(oldParent)
                  + " SET parent_id = ?, mod_metadata = ?, change_date = ?"
                  + " WHERE "
                  + IN_THIS_MAILBOX_AND
                  + relation);
      int pos = 1;
      if (newParent instanceof VirtualConversation) {
        stmt.setNull(pos++, Types.INTEGER);
      } else {
        stmt.setInt(pos++, newParent.getId());
      }
      stmt.setInt(pos++, mbox.getOperationChangeID());
      stmt.setInt(pos++, mbox.getOperationTimestamp());
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setInt(
          pos++,
          oldParent instanceof VirtualConversation
              ? ((VirtualConversation) oldParent).getMessageId()
              : oldParent.getId());
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw ServiceException.FAILURE(
          "writing new parent for children of item " + oldParent.getId(), e);
    } finally {
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public void openConversation(Mailbox mbox, String hash, int conversationId) throws ServiceException {
    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    try {
      stmt =
          conn.prepareStatement(
              DbUtil.upsert(
                      getConversationTableName(mbox),
                      withMailboxId("hash", "conv_id"),
                      withMailboxId("hash"),
                      List.of("conv_id")));
      int pos = 1;
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setString(pos++, hash);
      stmt.setInt(pos++, conversationId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw ServiceException.FAILURE("writing open conversation association for hash " + hash, e);
    } finally {
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public void closeConversation(Mailbox mbox, String hash, int conversationId) throws ServiceException {
    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    try {
      stmt =
          conn.prepareStatement(
              "DELETE FROM "
                  + getConversationTableName(mbox)
                  + " WHERE "
                  + IN_THIS_MAILBOX_AND
                  + "hash = ? AND conv_id = ?");
      int pos = 1;
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setString(pos++, hash);
      stmt.setInt(pos++, conversationId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw ServiceException.FAILURE("closing open conversation association for hash " + hash, e);
    } finally {
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public void closeOldConversations(Mailbox mbox, int beforeDate) throws ServiceException {
    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    ZimbraLog.purge.debug("Closing conversations dated before %d.", beforeDate);
    try {
      String mailboxJoin =
          " AND mi.mailbox_id = open_conversation.mailbox_id";
      stmt =
          conn.prepareStatement(
              "DELETE FROM "
                  + getConversationTableName(mbox)
                  + " WHERE "
                  + IN_THIS_MAILBOX_AND
                  + "conv_id IN ("
                  + "  SELECT id FROM "
                  + getMailItemTableName(mbox, "mi")
                  + "  WHERE mi.id = open_conversation.conv_id"
                  + mailboxJoin
                  + "  AND date < ?)");
      int pos = 1;
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setInt(pos++, beforeDate);
      int numRows = stmt.executeUpdate();
      if (numRows > 0) {
        ZimbraLog.purge.info("Closed %d conversations dated before %d.", numRows, beforeDate);
      }
    } catch (SQLException e) {
      throw ServiceException.FAILURE("closing open conversations dated before " + beforeDate, e);
    } finally {
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public void changeOpenTargets(MailItem oldTarget, int newTargetId)
      throws ServiceException {
    Mailbox mbox = oldTarget.getMailbox();
    int oldTargetId =
        oldTarget instanceof VirtualConversation
            ? ((VirtualConversation) oldTarget).getMessageId()
            : oldTarget.getId();

    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    try {
      stmt =
          conn.prepareStatement(
              "UPDATE "
                  + getConversationTableName(oldTarget)
                  + " SET conv_id = ? WHERE "
                  + IN_THIS_MAILBOX_AND
                  + "conv_id = ?");
      int pos = 1;
      stmt.setInt(pos++, newTargetId);
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setInt(pos++, oldTargetId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw ServiceException.FAILURE(
          "switching open conversation association for item " + oldTarget.getId(), e);
    } finally {
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public UnderlyingData getByHash(Mailbox mbox, String hash) throws ServiceException {
    return ListUtil.getFirstElement(getByHashes(mbox, Arrays.asList(hash)));
  }

  @Override
  public List<UnderlyingData> getByHashes(Mailbox mbox, List<String> hashes)
      throws ServiceException {
    if (ListUtil.isEmpty(hashes)) {
      return null;
    }

    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt =
          conn.prepareStatement(
              "SELECT "
                  + DB_FIELDS
                  + " FROM "
                  + getMailItemTableName(mbox, "mi")
                  + ", "
                  + getConversationTableName(mbox, "oc")
                  + " WHERE mi.id = oc.conv_id AND "
                  + DbUtil.whereIn("oc.hash", hashes.size())
                  + " AND oc.mailbox_id = ? AND mi.mailbox_id = oc.mailbox_id");
      int pos = 1;
      for (String hash : hashes) {
        stmt.setString(pos++, hash);
      }
      pos = setMailboxId(stmt, mbox, pos);
      rs = stmt.executeQuery();

      List<UnderlyingData> dlist = new ArrayList<>(3);
      Set<Integer> convIds = Sets.newHashSetWithExpectedSize(3);
      while (rs.next()) {
        int id = rs.getInt(CI_ID);
        if (convIds.contains(id)) {
          continue;
        }

        UnderlyingData data = constructItem(rs);
        if (data.type == MailItem.Type.CONVERSATION.toByte()) {
          completeConversation(mbox, conn, data);
        }
        dlist.add(data);
        convIds.add(data.id);
      }
      return dlist.isEmpty() ? null : dlist;
    } catch (SQLException e) {
      throw ServiceException.FAILURE("fetching conversation for hash " + hashes, e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
    }
  }

  @Override
  public TypedIdList listConvItems(
      Folder folder, long messageSyncStart, boolean descending, boolean older)
      throws ServiceException {
    Mailbox mbox = folder.getMailbox();
    TypedIdList result = new TypedIdList();
    DbConnection conn = mbox.getOperationConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      if (older) {
        stmt =
            conn.prepareStatement(
                "SELECT parent_id  FROM "
                    + getMailItemTableName(folder)
                    + " WHERE "
                    + IN_THIS_MAILBOX_AND
                    + " type = ? AND date < ?"
                    + " ORDER BY date"
                    + (descending ? " DESC" : ""));
      } else {
        stmt =
            conn.prepareStatement(
                "SELECT parent_id  FROM "
                    + getMailItemTableName(folder)
                    + " WHERE "
                    + IN_THIS_MAILBOX_AND
                    + " type = ? AND date >= ?"
                    + " ORDER BY date"
                    + (descending ? " DESC" : ""));
      }
      int pos = 1;
      pos = setMailboxId(stmt, mbox, pos);
      stmt.setByte(
          pos++, MailItem.Type.MESSAGE.toByte());
      stmt.setLong(pos++, messageSyncStart);
      rs = stmt.executeQuery();

      while (rs.next()) {
        int id = rs.getInt(1);
        if (id != 0 && !result.contains(id)) {
          result.add(MailItem.Type.CONVERSATION, id, "");
        }
      }
      return result;
    } catch (SQLException e) {
      throw ServiceException.FAILURE("fetching item list for folder " + folder.getId(), e);
    } finally {
      DbPool.closeResults(rs);
      DbPool.closeStatement(stmt);
    }
  }
}

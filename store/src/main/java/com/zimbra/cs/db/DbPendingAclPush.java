// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.util.ZimbraApplication;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DbPendingAclPush {

    public static String TABLE_PENDING_ACL_PUSH = "pending_acl_push";

    private static boolean supported;

    static {
         supported = ZimbraApplication.getInstance().supports(DbPendingAclPush.class);
    }

    public static void queue(Mailbox mbox, int itemId) throws ServiceException {
        if (!supported)
            return;
        if (mbox == null)
            return;
        ZimbraLog.mailbox.debug("Queuing for ACL push - mailbox %s item %s", mbox.getId(), itemId);
        DbConnection conn = mbox.getOperationConnection();
        PreparedStatement stmt = null;
        try {
        	
        	stmt = conn.prepareStatement(
                    "SELECT mailbox_id, item_id, date FROM " + TABLE_PENDING_ACL_PUSH + " WHERE mailbox_id = ? AND item_id = ? ");
            stmt.setInt(1, mbox.getId());
            stmt.setInt(2, itemId);
            ResultSet rs = stmt.executeQuery();
            
			if (rs.next()) {
				stmt = conn.prepareStatement("UPDATE " + TABLE_PENDING_ACL_PUSH
						+ " SET date = ? WHERE mailbox_id = ? AND item_id = ?");

				stmt.setLong(1, System.currentTimeMillis());
				stmt.setInt(2, mbox.getId());
				stmt.setInt(3, itemId);
				stmt.executeUpdate();
			} else {
        	
	            String command = "REPLACE";
	            stmt = conn.prepareStatement(
	                    command + " INTO " + TABLE_PENDING_ACL_PUSH + " (mailbox_id, item_id, date) VALUES (?, ?, ?)");
	            stmt.setInt(1, mbox.getId());
	            stmt.setInt(2, itemId);
	            stmt.setLong(3, System.currentTimeMillis());
	            stmt.executeUpdate();
            }
			
        } catch (SQLException e) {
            throw ServiceException.FAILURE(
                    "Unable to queue for ACL push - mailbox " + mbox.getId() + " item " + itemId, e);
        } finally {
            conn.closeQuietly(stmt);
        }
    }

    public record Entry(int mailboxId, int itemId, long date) {}

    public static List<Entry> getEntries() throws ServiceException {
        List<Entry> entries = new ArrayList<>();
        DbConnection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ZimbraLog.misc.debug("Getting entries for ACL push");
        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT mailbox_id, item_id, date FROM " + TABLE_PENDING_ACL_PUSH);
            rs = stmt.executeQuery();
            while (rs.next()) {
                entries.add(new Entry(rs.getInt(1), rs.getInt(2), rs.getLong(3)));
            }
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to get entries for ACL push", e);
        } finally {
            DbPool.closeResults(rs);
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
        return entries;
    }

    public static void deleteEntries(Collection<Entry> entries) throws ServiceException {
        if (entries.isEmpty()) {
            return;
        }
        ZimbraLog.misc.debug("Deleting %d entries for ACL push", entries.size());
        DbConnection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                    "DELETE FROM " + TABLE_PENDING_ACL_PUSH + " WHERE mailbox_id = ? AND item_id = ? AND date = ?");
            for (Entry entry : entries) {
                stmt.setInt(1, entry.mailboxId());
                stmt.setInt(2, entry.itemId());
                stmt.setLong(3, entry.date());
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to delete entries for ACL push", e);
        } finally {
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

    public static void deleteEntry(int mailboxId, int itemId) throws ServiceException {
        ZimbraLog.misc.debug("Deleting entry for ACL push for mailbox: %d, item : %d", mailboxId, itemId);
        DbConnection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DbPool.getConnection();
            stmt = conn.prepareStatement(
                    "DELETE FROM " + TABLE_PENDING_ACL_PUSH + " WHERE mailbox_id=? AND" +
                    		" item_id=?");
            stmt.setInt(1, mailboxId);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw ServiceException.FAILURE("Unable to delete UID's", e);
        } finally {
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }
}

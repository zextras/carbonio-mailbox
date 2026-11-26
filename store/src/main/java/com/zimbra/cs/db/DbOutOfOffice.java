// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.db;

import com.zextras.mailbox.outofoffice.OutOfOfficeRepository;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.db.DbPool.DbConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class DbOutOfOffice implements OutOfOfficeRepository {

    private static final String TABLE_NAME = "out_of_office";
    private static final Log LOG = LogFactory.getLog(DbOutOfOffice.class);

    @Override
    public boolean alreadySent(int mailboxId, String sentTo, Instant since) throws ServiceException {
        String recipient = sentTo.toLowerCase();
        DbConnection conn = DbPool.getConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM " + TABLE_NAME +
                    " WHERE " + DbMailItem.IN_THIS_MAILBOX_AND + "sent_to = ? AND sent_on > ?");
            int pos = 1;
            pos = DbMailItem.setMailboxId(stmt, mailboxId, pos);
            stmt.setString(pos++, recipient);
            stmt.setTimestamp(pos++, Timestamp.from(since));
            rs = stmt.executeQuery();
            rs.next();
            boolean result = rs.getInt(1) > 0;
            LOG.debug("DbOutOfOffice.alreadySent() returning %s. mailbox_id=%d, sent_to='%s' since=%s",
                    result, mailboxId, recipient, since);
            return result;
        } catch (SQLException e) {
            throw ServiceException.FAILURE("DbOutOfOffice.alreadySent: sql exception (mailbox_id="
                    + mailboxId + " sent_to=" + recipient + ")", e);
        } finally {
            DbPool.closeResults(rs);
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

    @Override
    public void setSentTime(int mailboxId, String sentTo, Instant sentOn) throws ServiceException {
        DbConnection conn = DbPool.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(DbUtil.upsert(TABLE_NAME,
                    DbMailItem.withMailboxId("sent_to", "sent_on"), DbMailItem.withMailboxId("sent_to"), List.of("sent_on")));
            int pos = 1;
            pos = DbMailItem.setMailboxId(stmt, mailboxId, pos);
            stmt.setString(pos++, sentTo.toLowerCase());
            stmt.setTimestamp(pos++, Timestamp.from(sentOn));
            int num = stmt.executeUpdate();
            conn.commit();
            if (num > 0) {
                LOG.debug("DbOutOfOffice.setSentTime: ok (mailbox_id=%d sent_to=%s sent_on=%s rows=%d)",
                        mailboxId, sentTo, sentOn, num);
            } else {
                LOG.error("DbOutOfOffice.setSentTime: no rows updated (mailbox_id=" + mailboxId
                        + " sent_to=" + sentTo + " sent_on=" + sentOn + " rows=" + num + ")");
            }
        } catch (SQLException e) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("DbOutOfOffice.setSentTime: sql exception " +
                    "(mailbox_id=" + mailboxId + " sent_to" + sentTo + " sent_on=" + sentOn + ")", e);
        } finally {
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

    @Override
    public void clear(int mailboxId) throws ServiceException {
        DbConnection conn = DbPool.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE mailbox_id = ?");
            DbMailItem.setMailboxId(stmt, mailboxId, 1);
            int num = stmt.executeUpdate();
            conn.commit();
            LOG.debug("DbOutOfOffice.clear() mbox=%d rows=%d", mailboxId, num);
        } catch (SQLException e) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("DbOutOfOffice.clear mailbox_id=" + mailboxId, e);
        } finally {
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }

    @Override
    public void prune(Instant before) throws ServiceException {
        DbConnection conn = DbPool.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE sent_on <= ?");
            stmt.setTimestamp(1, Timestamp.from(before));
            int num = stmt.executeUpdate();
            conn.commit();
            LOG.debug("DbOutOfOffice.prune() deleted %d rows with cutoff <= %s", num, before);
        } catch (SQLException e) {
            DbPool.quietRollback(conn);
            throw ServiceException.FAILURE("DbOutOfOffice.prune()", e);
        } finally {
            DbPool.closeStatement(stmt);
            DbPool.quietClose(conn);
        }
    }
}

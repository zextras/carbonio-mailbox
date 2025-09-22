// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.db.Db;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.db.HSQLDB;
import com.zimbra.cs.mailbox.Mailbox.MailboxData;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.redolog.op.RedoableOp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DesktopMailboxTest extends MailboxTestSuite {

	@BeforeAll
	public static void init() throws Exception {
		MailboxManager.setInstance(new MailboxManager() {
			@Override
			protected Mailbox instantiateMailbox(MailboxData data) {
				//mock the behaviors we need to test in DesktopMailbox
				return new Mailbox(data) {
					@Override
					protected boolean needRedo(OperationContext octxt, RedoableOp recorder) {
						return false;
					}
				};
			}
		});
	}


	private int countInboxMessages(Mailbox mbox) throws ServiceException, SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		DbConnection conn = DbPool.getConnection(mbox);
		try {
			stmt = conn.prepareStatement(
					"SELECT COUNT(*) FROM mboxgroup1.mail_item WHERE mailbox_id = ? and folder_id = ?");
			stmt.setInt(1, mbox.getId());
			stmt.setInt(2, Mailbox.ID_FOLDER_INBOX);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		} finally {
			DbPool.closeResults(rs);
			DbPool.quietCloseStatement(stmt);
			DbPool.quietClose(conn);
		}
	}

	@Test
	void nestedTxn() throws Exception {
		Account acct = createAccount().create();
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct);
		HSQLDB db = (HSQLDB) Db.getInstance();
		db.useMVCC(mbox);

		DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX);
		mbox.lock.lock();
		try {
			OperationContext octx = new OperationContext(acct);
			mbox.beginTransaction("outer", octx);
			mbox.beginTransaction("inner1", octx);
			mbox.addMessage(octx, new ParsedMessage("From: test1-1@sub1.zimbra.com".getBytes(), false),
					dopt, null);

			//nothing committed yet
			assertEquals(0, countInboxMessages(mbox));
			mbox.endTransaction(true); //inner 1

			//nothing committed yet
			assertEquals(0, countInboxMessages(mbox));

			mbox.beginTransaction("inner2", null);
			mbox.addMessage(null, new ParsedMessage("From: test1-2@sub1.zimbra.com".getBytes(), false),
					dopt, null);

			//nothing committed yet
			assertEquals(0, countInboxMessages(mbox));
			mbox.endTransaction(true); //inner 2

			//nothing committed yet
			assertEquals(0, countInboxMessages(mbox));

			mbox.endTransaction(true); //outer

			//committed
			assertEquals(2, countInboxMessages(mbox));
		} finally {
			mbox.lock.release();
		}
	}
}

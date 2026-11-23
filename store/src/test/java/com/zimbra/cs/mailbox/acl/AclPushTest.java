// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.acl;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPendingAclPush;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbResults;
import com.zimbra.cs.db.DbUtil;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * @author zimbra
 *
 */


public class AclPushTest extends MailboxTestSuite {

	private static Account owner1;
	private static Account principal1;
	private static Account owner2;
	private static Account principal2;
	private DbConnection connection;

	@BeforeAll
	public static void init() throws Exception {
		owner1 = createAccount().withUsername("owner1").withPassword("secret").create();
		principal1 = createAccount().withUsername("principal1").withPassword("secret")
				.create();
		owner2 = createAccount().withUsername("owner2").withPassword("secret").create();
		principal2 = createAccount().withUsername("principal2").withPassword("secret")
				.create();
	}



 @Test
 void getAclPushEntriesMultipleGrantForSameItem() throws Exception {

  Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner1);

  Folder folder = mbox.createFolder(null, "shared",
    new Folder.FolderOptions()
      .setDefaultView(Type.FOLDER));

  OperationContext octxt = new OperationContext(owner1);
  List<DbPendingAclPush.Entry> mboxIdToItemIds = null;
  mbox.lock.lock();
  try {
   mbox.grantAccess(octxt, folder.getId(), principal1.getId(),
     ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
   mbox.grantAccess(octxt, folder.getId(), principal1.getId(),
     ACL.GRANTEE_USER, ACL.stringToRights("rw"), null);

   mboxIdToItemIds = DbPendingAclPush
     .getEntries();
  } finally {
   mbox.lock.release();
  }

  Thread.sleep(1000);
  mboxIdToItemIds = DbPendingAclPush.getEntries();
  assertEquals(mboxIdToItemIds.size(), 0);
  short rights = folder.getACL().getGrantedRights(principal1);
  assertEquals(3, rights);
 }


 @Test
 void getAclPushEntriesFolderNameWithSemiColon() throws Exception {

  try {
		Account owner = owner2;
		Account grantee = principal2;
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner);

		Folder folder = mbox.createFolder(null, "shared",
				new Folder.FolderOptions()
						.setDefaultView(Type.FOLDER));
		Folder folder2 = mbox.createFolder(null, "shared; hello",
				new Folder.FolderOptions()
						.setDefaultView(Type.FOLDER));

		OperationContext octxt = new OperationContext(owner);
		List<DbPendingAclPush.Entry> mboxIdToItemIds = null;

		mbox.lock.lock();
		try {
			mbox.grantAccess(octxt, folder.getId(), grantee.getId(),
					ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
			mbox.grantAccess(octxt, folder2.getId(), grantee.getId(),
					ACL.GRANTEE_USER, ACL.stringToRights("rw"), null);

			mboxIdToItemIds = DbPendingAclPush
					.getEntries();
		} finally {
			mbox.lock.release();
		}
//		assertTrue(mboxIdToItemIds.size() == 2);

		Thread.sleep(1000);
		mboxIdToItemIds = DbPendingAclPush.getEntries();
		assertEquals(mboxIdToItemIds.size(), 0);
	} finally {

	}
 }

  @Test
  void pushesEntryNotOlderThanThePushTime() throws Exception {
    Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner1);
    Folder folder = mbox.createFolder(null, "pushed later",
        new Folder.FolderOptions().setDefaultView(Type.FOLDER));
    synchronized (AclPushTask.class) {
      mbox.grantAccess(new OperationContext(owner1), folder.getId(), principal1.getId(),
          ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
      AclPushTask.doWork();
      insertEntry(mbox.getId(), folder.getId(), System.currentTimeMillis() + 60_000);

      AclPushTask.doWork();

      assertTrue(entriesFor(mbox.getId(), folder.getId()).isEmpty());
      assertTrue(isShared(owner1, folder.getId()));
    }
  }

  @Test
  void keepsEntryQueuedAgainAfterItWasRead() throws Exception {
    int mailboxId = MailboxManager.getInstance().getMailboxByAccount(owner2).getId();
    int itemId = 9999;
    synchronized (AclPushTask.class) {
      insertEntry(mailboxId, itemId, 1000L);
      List<DbPendingAclPush.Entry> read = DbPendingAclPush.getEntries();
      updateEntryDate(mailboxId, itemId, 2000L);

      DbPendingAclPush.deleteEntries(read);

      assertEquals(List.of(new DbPendingAclPush.Entry(mailboxId, itemId, 2000L)),
          entriesFor(mailboxId, itemId));
      DbPendingAclPush.deleteEntry(mailboxId, itemId);
    }
  }

  private static List<DbPendingAclPush.Entry> entriesFor(int mailboxId, int itemId) throws Exception {
    DbConnection conn = DbPool.getConnection();
    try {
      DbResults results = DbUtil.executeQuery(conn,
          "SELECT date FROM " + DbPendingAclPush.TABLE_PENDING_ACL_PUSH
              + " WHERE mailbox_id = ? AND item_id = ?", mailboxId, itemId);
      List<DbPendingAclPush.Entry> entries = new ArrayList<>();
      while (results.next()) {
        entries.add(new DbPendingAclPush.Entry(mailboxId, itemId, ((Number) results.getObject(1)).longValue()));
      }
      return entries;
    } finally {
      conn.closeQuietly();
    }
  }

  private static boolean isShared(Account owner, int folderId) throws Exception {
    Account account = Provisioning.getInstance().getAccountById(owner.getId());
    for (String sharedItem : account.getSharedItem()) {
      if (AclPushSerializer.deserialize(sharedItem).getItemId() == folderId) {
        return true;
      }
    }
    return false;
  }

  private static void insertEntry(int mailboxId, int itemId, long date) throws Exception {
    execute("INSERT INTO " + DbPendingAclPush.TABLE_PENDING_ACL_PUSH
        + " (mailbox_id, item_id, date) VALUES (?, ?, ?)", mailboxId, itemId, date);
  }

  private static void updateEntryDate(int mailboxId, int itemId, long date) throws Exception {
    execute("UPDATE " + DbPendingAclPush.TABLE_PENDING_ACL_PUSH
        + " SET date = ? WHERE mailbox_id = ? AND item_id = ?", date, mailboxId, itemId);
  }

  private static void execute(String sql, Object... params) throws Exception {
    DbConnection conn = DbPool.getConnection();
    try {
      DbUtil.executeUpdate(conn, sql, params);
      conn.commit();
    } finally {
      conn.closeQuietly();
    }
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.acl;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Multimap;
import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.db.DbPendingAclPush;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.OperationContext;
import java.util.Date;
import org.junit.jupiter.api.BeforeAll;
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
  Multimap<Integer, Integer> mboxIdToItemIds = null;
  mbox.lock.lock();
  try {
   mbox.grantAccess(octxt, folder.getId(), principal1.getId(),
     ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
   mbox.grantAccess(octxt, folder.getId(), principal1.getId(),
     ACL.GRANTEE_USER, ACL.stringToRights("rw"), null);

   mboxIdToItemIds = DbPendingAclPush
     .getEntries(new Date());
  } finally {
   mbox.lock.release();
  }

  Thread.sleep(1000);
  mboxIdToItemIds = DbPendingAclPush.getEntries(new Date());
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
		Multimap<Integer, Integer> mboxIdToItemIds = null;

		mbox.lock.lock();
		try {
			mbox.grantAccess(octxt, folder.getId(), grantee.getId(),
					ACL.GRANTEE_USER, ACL.stringToRights("r"), null);
			mbox.grantAccess(octxt, folder2.getId(), grantee.getId(),
					ACL.GRANTEE_USER, ACL.stringToRights("rw"), null);

			mboxIdToItemIds = DbPendingAclPush
					.getEntries(new Date());
		} finally {
			mbox.lock.release();
		}
//		assertTrue(mboxIdToItemIds.size() == 2);

		Thread.sleep(1000);
		mboxIdToItemIds = DbPendingAclPush.getEntries(new Date());
		assertEquals(mboxIdToItemIds.size(), 0);
	} finally {

	}
 }


}

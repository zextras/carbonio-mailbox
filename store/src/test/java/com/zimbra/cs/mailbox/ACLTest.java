// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.GuestAccount;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ACLTest extends MailboxTestSuite {

	private static Account owner;
	private static Account grantee;

	@BeforeAll
	public static void init() throws Exception {
		owner = createAccount().create();
		grantee = createAccount().create();
	}

	@Test
	void testRegrant() throws Exception {
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner);

		Folder folder = mbox.createFolder(null, "shared1",
				new Folder.FolderOptions().setDefaultView(Type.FOLDER));
		OperationContext octxt = new OperationContext(owner);
		mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER,
				ACL.stringToRights("r"), null);
		try {
			mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER,
					ACL.stringToRights("r"), null);
			fail("regrant has succeeded");
		} catch (ServiceException se) {
			if (!se.getCode().equals(MailServiceException.GRANT_EXISTS)) {
				fail("regrant throws ServiceException with code " + se.getCode());
			}
		}
	}

	@Test
	void testRegrantDifferentPermission() throws Exception {
		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner);

		Folder folder = mbox.createFolder(null, "shared",
				new Folder.FolderOptions().setDefaultView(Type.FOLDER));
		OperationContext octxt = new OperationContext(owner);
		mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER,
				ACL.stringToRights("r"), null);
		try {
			mbox.grantAccess(octxt, folder.getId(), grantee.getId(), ACL.GRANTEE_USER,
					ACL.stringToRights("rw"), null);
		} catch (ServiceException se) {
			if (!se.getCode().equals(MailServiceException.GRANT_EXISTS)) {
				fail("regrant throws ServiceException with code " + se.getCode());
			}
			fail("regrant has failed");
		}
	}

	@Test
	void testPublicAccess() throws Exception {
		owner.setExternalSharingEnabled(false);
		Account guestUser = GuestAccount.ANONYMOUS_ACCT;

		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(owner);

		Folder folder = mbox.createFolder(null, "sharedCalender",
				new Folder.FolderOptions().setDefaultView(MailItem.Type.APPOINTMENT));
		OperationContext octxt = new OperationContext(owner);
		mbox.grantAccess(octxt, folder.getId(), guestUser.getId(), ACL.GRANTEE_PUBLIC,
				ACL.stringToRights("r"), null);

		UnderlyingData underlyingData = new UnderlyingData();
		underlyingData.setSubject("test subject");
		underlyingData.folderId = folder.getId();
		underlyingData.name = "name";
		underlyingData.type = MailItem.Type.APPOINTMENT.toByte();
		underlyingData.uuid = owner.getUid();
		underlyingData.parentId = folder.getId();
		underlyingData.setBlobDigest("test digest");

		CalendarItem calendarItem = new Appointment(mbox, underlyingData, true);
		assertTrue(calendarItem.canAccess(ACL.RIGHT_READ, guestUser, false));
	}
}

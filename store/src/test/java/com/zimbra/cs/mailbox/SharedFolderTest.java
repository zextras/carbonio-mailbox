// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SharedFolderTest extends MailboxTestSuite {

	private static Account owner;
	private static Account grantee;
	private static Account virtualGrantee;

	@BeforeAll
	public static void init() throws Exception {
		owner = createAccount().create();
		grantee = createAccount().create();
		virtualGrantee = createAccount().withAttribute(
				Provisioning.A_zimbraIsExternalVirtualAccount, "TRUE").create();
	}

	@Test
	void adminGrant() throws Exception {

		Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(owner.getId());

		Folder.FolderOptions fopt = new Folder.FolderOptions();
		fopt.setDefaultView(MailItem.Type.MESSAGE);
		Folder f1 = mbox.createFolder(null, "/Inbox/f1", fopt);

		String exception;

		// allow granting admin right to internal user
		try {
			exception = null;
			mbox.grantAccess(null, f1.getId(), grantee.getId(), ACL.GRANTEE_USER,
					ACL.stringToRights("rwidxa"), null);
		} catch (ServiceException e) {
			exception = e.getCode();
		}
		assertNull(exception, "got wrong exception");

		// don't allow granting admin right to virtual account
		try {
			exception = null;
			mbox.grantAccess(null, f1.getId(), virtualGrantee.getId(), ACL.GRANTEE_USER,
					ACL.stringToRights("rwidxa"), null);
		} catch (ServiceException e) {
			exception = e.getCode();
		}
		assertEquals(MailServiceException.CANNOT_GRANT, exception, "got wrong exception");

		// don't allow granting admin right to public
		try {
			exception = null;
			mbox.grantAccess(null, f1.getId(), null, ACL.GRANTEE_PUBLIC, ACL.stringToRights("rwidxa"),
					null);
		} catch (ServiceException e) {
			exception = e.getCode();
		}
		assertEquals(MailServiceException.CANNOT_GRANT, exception, "got wrong exception");

		// allow granting non-admin rights to virtual account
		try {
			exception = null;
			mbox.grantAccess(null, f1.getId(), virtualGrantee.getId(), ACL.GRANTEE_USER,
					ACL.stringToRights("rwidx"), null);
		} catch (ServiceException e) {
			exception = e.getCode();
		}
		assertNull(exception, "got wrong exception");

		// allow granting non-admin rights to public
		try {
			exception = null;
			mbox.grantAccess(null, f1.getId(), null, ACL.GRANTEE_PUBLIC, ACL.stringToRights("rwidx"),
					null);
		} catch (ServiceException e) {
			exception = e.getCode();
		}
		assertNull(exception, "got wrong exception");
	}
}

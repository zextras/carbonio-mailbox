// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ImapPathTest extends MailboxTestSuite {

	private Account acct;

	@BeforeEach
	public void setUp() throws Exception {
		acct = createAccount().create();
	}


	@Test
	void testWildCardStar() throws Exception {
		ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
		ImapPath i4Path = new ImapPath("*", credentials, ImapPath.Scope.UNPARSED);
		assertNotNull(i4Path, "Should be able to instantiate ImapPath for '*'");
		String owner = i4Path.getOwner();
		assertNull(owner, "owner part of the path should be null. Was " + owner);
		assertTrue(i4Path.belongsTo(credentials),
				"belongsTo should return TRUE with same credentials as were passed to the constructor");
		assertEquals(acct.toString(), i4Path.getOwnerAccount().toString(), "Incorrect owner account");
		assertEquals("\"*\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
	}

	@Test
	void testWildCardPercent() throws Exception {
		ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
		ImapPath i4Path = new ImapPath("%", credentials, ImapPath.Scope.UNPARSED);
		assertNotNull(i4Path, "Should be able to instantiate ImapPath for '*'");
		String owner = i4Path.getOwner();
		assertNull(owner, "owner part of the path should be null. Was " + owner);
		assertTrue(i4Path.belongsTo(credentials),
				"belongsTo should return TRUE with same credentials as were passed to the constructor");
		assertEquals(acct.toString(), i4Path.getOwnerAccount().toString(), "Incorrect owner account");
		assertEquals("\"%\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
	}

	@Test
	void testWildCardPercent2() throws Exception {
		ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
		ImapPath i4Path = new ImapPath("%/%", credentials, ImapPath.Scope.UNPARSED);
		assertNotNull(i4Path, "Should be able to instantiate ImapPath for '*'");
		String owner = i4Path.getOwner();
		assertNull(owner, "owner part of the path should be null. Was " + owner);
		assertTrue(i4Path.belongsTo(credentials),
				"belongsTo should return TRUE with same credentials as were passed to the constructor");
		assertEquals(acct.toString(), i4Path.getOwnerAccount().toString(), "Incorrect owner account");
		assertEquals("\"%/%\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
	}

	@Test
	void testHomeWildCard() throws Exception {
		ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
		ImapPath i4Path = new ImapPath("/home/*", credentials, ImapPath.Scope.UNPARSED);
		assertNotNull(i4Path, "Should be able to instantiate ImapPath for '/home/*'");
		String owner = i4Path.getOwner();
		assertNull(owner, "owner part of the path should be null. Was " + owner);
		assertTrue(i4Path.belongsTo(credentials),
				"belongsTo should return TRUE with same credentials as were passed to the constructor");
		assertEquals(acct.toString(), i4Path.getOwnerAccount().toString(), "Incorrect owner account");
		assertEquals("\"home/*\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
	}

	@Test
	void testHomePercent() throws Exception {
		ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
		ImapPath i4Path = new ImapPath("/home/%", credentials, ImapPath.Scope.UNPARSED);
		assertNotNull(i4Path, "Should be able to instantiate ImapPath for '/home/%'");
		String owner = i4Path.getOwner();
		assertNull(owner, "owner part of the path should be null. Was " + owner);
		assertTrue(i4Path.belongsTo(credentials),
				"belongsTo should return TRUE with same credentials as were passed to the constructor");
		assertEquals(acct.toString(), i4Path.getOwnerAccount().toString(), "Incorrect owner account");
		assertEquals("\"home/%\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
	}

	@Test
	void testHomePercent2() throws Exception {
		ImapCredentials credentials = new ImapCredentials(acct, ImapCredentials.EnabledHack.NONE);
		ImapPath i4Path = new ImapPath("/home/%/%", credentials, ImapPath.Scope.UNPARSED);
		assertNotNull(i4Path, "Should be able to instantiate ImapPath for '/home/%/%'");
		String owner = i4Path.getOwner();
		assertNull(owner, "owner part of the path should be null. Was " + owner);
		assertTrue(i4Path.belongsTo(credentials),
				"belongsTo should return TRUE with same credentials as were passed to the constructor");
		assertEquals(acct.toString(), i4Path.getOwnerAccount().toString(), "Incorrect owner account");
		assertEquals("\"home/%/%\"", i4Path.asUtf7String(), "Incorrect UTF7-encoded path");
	}
}

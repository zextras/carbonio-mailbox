// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.pop3;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.server.ServerThrottle;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class Pop3HandlerTest extends MailboxTestSuite {


	private String LOCAL_USER;
	private Account acct;
	private final String password = "secret";

	@BeforeAll
	public static void init() throws Exception {
		String[] hosts = {"localhost", "127.0.0.1"};
		ServerThrottle.configureThrottle(new Pop3Config(false).getProtocol(), 100, 100,
				Arrays.asList(hosts), Arrays.asList(hosts));
	}

	@BeforeEach
	public void setUp() throws Exception {
		acct = createAccount().withPassword(password).create();
		LOCAL_USER = acct.getName();
	}

	@Test
	void testLogin3() throws Exception {
		Pop3Handler handler = new MockPop3Handler();

		acct.setPop3Enabled(true);
		acct.setPrefPop3Enabled(true);
		handler.authenticate(LOCAL_USER, null, password, null);
		assertEquals(Pop3Handler.STATE_TRANSACTION, handler.state);
	}

	@Test
	void testLogin4() throws Exception {
		assertThrows(Pop3CmdException.class, () -> {
			Pop3Handler handler = new MockPop3Handler();

			acct.setPop3Enabled(true);
			acct.setPrefPop3Enabled(false);
			handler.authenticate(LOCAL_USER, null, password, null);
		});
	}

	@Test
	void testLogin7() throws Exception {
		assertThrows(Pop3CmdException.class, () -> {
			Pop3Handler handler = new MockPop3Handler();

			acct.setPop3Enabled(false);
			acct.setPrefPop3Enabled(true);
			handler.authenticate(LOCAL_USER, null, password, null);
		});
	}

	@Test
	void testLogin8() throws Exception {
		assertThrows(Pop3CmdException.class, () -> {
			Pop3Handler handler = new MockPop3Handler();

			acct.setPop3Enabled(false);
			acct.setPrefPop3Enabled(false);
			handler.authenticate(LOCAL_USER, null, password, null);
		});
	}
}


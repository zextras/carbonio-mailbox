// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ZimbraAuthToken}.
 *
 * @author ysasaki
 */
public class ZimbraAuthTokenTest extends MailboxTestSuite {

	private Account account;

	@BeforeEach
	void setUp() throws Exception {
		account = createAccount().create();
	}

	@Test
	void test() throws Exception {
		ZimbraAuthToken at = new ZimbraAuthToken(account);
		long start = System.currentTimeMillis();
		String encoded = at.getEncoded();
		for (int i = 0; i < 1000; i++) {
			new ZimbraAuthToken(encoded);
		}
		System.out.println("Encoded 1000 auth-tokens elapsed=" + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			ZimbraAuthToken.getAuthToken(encoded);
		}
		System.out.println("Decoded 1000 auth-tokens elapsed=" + (System.currentTimeMillis() - start));
	}

	@Test
	void testEncodedDifferentOnTokenIDReset() throws Exception {
		ZimbraAuthToken at = new ZimbraAuthToken(account);
		ZimbraAuthToken clonedAuthToken = at.clone();
		clonedAuthToken.resetTokenId();
		assertNotEquals(at.getEncoded(), clonedAuthToken.getEncoded());
	}

}

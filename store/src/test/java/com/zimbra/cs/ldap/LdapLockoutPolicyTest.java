// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3

package com.zimbra.cs.ldap;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapLockoutPolicy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LdapLockoutPolicyTest extends MailboxTestSuite {

	private static Account acct;

	@BeforeAll
	public static void init() throws Exception {
		acct = createAccount().create();
	}


	/**
	 * Test method for {@link com.zimbra.cs.account.ldap.LdapLockoutPolicy#failedLogin()}.
	 */
	@Test
	void testFailedLogin() throws Exception {
		acct.setPasswordLockoutEnabled(true);
		acct.setPasswordLockoutMaxFailures(2);
		acct.setPasswordLockoutFailureLifetime("120s");

		// First failure
		LdapLockoutPolicy lockoutPolicy = new LdapLockoutPolicy(Provisioning.getInstance(), acct);
		lockoutPolicy.failedLogin();
		// failure time is updated
		assertEquals(1, acct.getPasswordLockoutFailureTimeAsString().length);

		// second Failure
		lockoutPolicy = new LdapLockoutPolicy(Provisioning.getInstance(), acct);
		lockoutPolicy.failedLogin();
		String[] failureTime = acct.getPasswordLockoutFailureTimeAsString();
		// failure time is updated
		assertEquals(2, failureTime.length);

		// account should be locked after two failure attempts
		assertTrue(acct.getAccountStatus().isLockout());

		// Third failure
		lockoutPolicy = new LdapLockoutPolicy(Provisioning.getInstance(), acct);
		lockoutPolicy.failedLogin();

		// Third failure attempt should not update failure time
		assertEquals(failureTime[0], acct.getPasswordLockoutFailureTimeAsString()[0]);
		assertEquals(failureTime[1], acct.getPasswordLockoutFailureTimeAsString()[1]);
	}
}

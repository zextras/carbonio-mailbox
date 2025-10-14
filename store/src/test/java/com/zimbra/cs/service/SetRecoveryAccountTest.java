// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.account.ZAttrProvisioning.FeatureResetPasswordStatus;
import com.zimbra.common.account.ZAttrProvisioning.PrefPasswordRecoveryAddressStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.service.mail.DirectInsertionMailboxManager;
import com.zimbra.cs.service.mail.ServiceTestUtil;
import com.zimbra.cs.service.mail.SetRecoveryAccount;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.SetRecoveryAccountRequest;
import com.zimbra.soap.type.Channel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class SetRecoveryAccountTest extends MailboxTestSuite {

	@BeforeAll
	public static void setUp() throws Exception {
		MailboxManager.setInstance(new DirectInsertionMailboxManager());
	}

	@Test
	void testMissingChannel() throws Exception {
		var recoveryAccount = createAccount().create();
		var testAccount5035 = createAccount()
				.withAttribute(
						Provisioning.A_zimbraFeatureResetPasswordStatus,
						FeatureResetPasswordStatus.enabled.toString())
				.create();
		testAccount5035.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
		SetRecoveryAccountRequest request = new SetRecoveryAccountRequest();
		request.setOp(SetRecoveryAccountRequest.Op.sendCode);
		request.setRecoveryAccount(recoveryAccount.getName());
		Element req = JaxbUtil.jaxbToElement(request);
		try {
			new SetRecoveryAccount().handle(req, ServiceTestUtil.getRequestContext(testAccount5035));
		} catch (ServiceException e) {
			fail("Exception should not be thrown\n" + e.getMessage());
		}
	}

	@Test
	void test4797() throws Exception {
		var recoveryAccount = createAccount().create();
		final Account testAccount4797 = createAccount()
				.withAttribute(
						Provisioning.A_zimbraFeatureResetPasswordStatus,
						FeatureResetPasswordStatus.enabled.toString())
				.create();
		Mailbox recoveryMailbox = MailboxManager.getInstance().getMailboxByAccount(recoveryAccount);
		testAccount4797.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
		assertNull(testAccount4797.getPrefPasswordRecoveryAddress());
		assertNull(testAccount4797.getPrefPasswordRecoveryAddressStatus());

		SetRecoveryAccountRequest request = new SetRecoveryAccountRequest();
		request.setOp(SetRecoveryAccountRequest.Op.sendCode);
		request.setRecoveryAccount(recoveryAccount.getName());
		request.setChannel(Channel.EMAIL);
		Element req = JaxbUtil.jaxbToElement(request);

		new SetRecoveryAccount().handle(req, ServiceTestUtil.getRequestContext(testAccount4797));

		// Refresh account to get updated attributes
		final Account refreshedAccount = Provisioning.getInstance().getAccountById(testAccount4797.getId());

		// Verify that the recovery email address is updated into ldap and
		// status is set to pending
		assertEquals(recoveryAccount.getName(), refreshedAccount.getPrefPasswordRecoveryAddress());
		assertEquals(
				PrefPasswordRecoveryAddressStatus.pending.toString(),
				refreshedAccount.getAttrs().get(Provisioning.A_zimbraPrefPasswordRecoveryAddressStatus));

		// Verify that recovery email address received the verification email
		Message msg = (Message) recoveryMailbox.getItemList(null, MailItem.Type.MESSAGE).get(0);
		assertEquals(
				"Request for recovery email address verification by " + testAccount4797.getName(),
				msg.getSubject());

		// Test that sending code again throws exception
		Account finalTestAccount479 = testAccount4797;
		ServiceException exception =
				assertThrows(
						ServiceException.class,
						() -> {
							new SetRecoveryAccount()
									.handle(req, ServiceTestUtil.getRequestContext(finalTestAccount479));
						});
		assertEquals(
				"service exception: Verification code already sent to this recovery email.",
				exception.getMessage());
	}

	@Test
	void test5035() throws Exception {
		var testAccount5035 = createAccount()
				.withAttribute(
						Provisioning.A_zimbraFeatureResetPasswordStatus,
						FeatureResetPasswordStatus.enabled.toString())
				.create();
		testAccount5035.setFeatureResetPasswordStatus(FeatureResetPasswordStatus.enabled);
		SetRecoveryAccountRequest request = new SetRecoveryAccountRequest();
		request.setOp(SetRecoveryAccountRequest.Op.sendCode);
		request.setRecoveryAccount(testAccount5035.getName()); // Same as primary account
		request.setChannel(Channel.EMAIL);
		Element req = JaxbUtil.jaxbToElement(request);

		ServiceException exception =
				assertThrows(
						ServiceException.class,
						() -> {
							new SetRecoveryAccount()
									.handle(req, ServiceTestUtil.getRequestContext(testAccount5035));
						});
		assertEquals(
				"service exception: Recovery address should not be same as primary/alias email address.",
				exception.getMessage());
	}
}

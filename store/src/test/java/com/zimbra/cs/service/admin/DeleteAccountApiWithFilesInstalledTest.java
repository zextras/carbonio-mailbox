/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.service.admin;

import static com.zimbra.cs.account.AccountServiceException.NO_SUCH_ACCOUNT;

import com.zextras.mailbox.soap.SoapExtension;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.MailboxTestUtil;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.account.AccountService;
import com.zimbra.cs.service.mail.MailServiceWithoutTracking;
import com.zimbra.soap.admin.message.DeleteAccountRequest;
import com.zimbra.soap.admin.message.GetAccountRequest;
import com.zimbra.soap.type.AccountSelector;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

class DeleteAccountApiWithFilesInstalledTest extends SoapTestSuite {

	@RegisterExtension
	static SoapExtension soapExtension = new SoapExtension.Builder()
			.addEngineHandler(AdminServiceWithFilesInstalled.class.getName())
			.addEngineHandler(AccountService.class.getName())
			.addEngineHandler(MailServiceWithoutTracking.class.getName())
			.create();

	private static MailboxTestUtil.AccountCreator.Factory accountCreatorFactory;

	@BeforeAll
	static void beforeAll() {
		Provisioning provisioning = Provisioning.getInstance();
		accountCreatorFactory = new MailboxTestUtil.AccountCreator.Factory(provisioning);
	}

	@Test
	void shouldNotDeleteAccountImmediately_WhenFilesIsInstalled() throws Exception {
		try (RabbitMQContainer messageBroker = new RabbitMQContainer(DockerImageName.parse(
				AdminServiceWithFilesInstalled.MESSAGE_BROKER_IMAGE))
				.withAdminPassword(AdminServiceWithFilesInstalled.MESSAGE_BROKER_PASSWORD)) {
			messageBroker.setPortBindings(
					List.of(AdminServiceWithFilesInstalled.MESSAGE_BROKER_PORT + ":5672"));
			messageBroker.start();
			final Account adminAccount = accountCreatorFactory.get().asGlobalAdmin().create();
			final Account userAccount = accountCreatorFactory.get().create();
			final String accountWithPublicShareId = userAccount.getId();

			final SoapResponse deleteAccountResponse = getSoapClient().newRequest()
					.setCaller(adminAccount).setSoapBody(new DeleteAccountRequest(accountWithPublicShareId))
					.call();
			Assertions.assertEquals(200, deleteAccountResponse.statusCode());

			final SoapResponse getAccountResponse = getSoapClient().newRequest()
					.setCaller(adminAccount)
					.setSoapBody(new GetAccountRequest(AccountSelector.fromId(accountWithPublicShareId)))
					.call();
			Assertions.assertEquals(200, getAccountResponse.statusCode());
			Assertions.assertFalse(getAccountResponse.body().contains(NO_SUCH_ACCOUNT));
		}
	}
}

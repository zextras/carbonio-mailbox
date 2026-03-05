package com.zextras.mailbox.api;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.util.CreateAccount.Factory;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.TestHttpClient.Response;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
public class AccountResourceIT {
	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	@Test
	void returnsAccountInfo() throws Exception {
		final Factory factory = new Factory(Provisioning.getInstance(), "test.com");
		final Account account = factory.get()
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"id\":\"" + account.getId() + "\""));
		assertTrue(response.body().contains("\"name\":\"" + account.getName() + "\""));
		assertTrue(response.body().contains("\"cosId\":\"" + account.getCOSId() + "\""));
		assertTrue(response.body().contains("\"isGlobalAdmin\":false"));
	}

	@Test
	void accountInfoIsGlobalAdmin() throws Exception {
		final Factory factory = new Factory(Provisioning.getInstance(), "test.com");
		final Account account = factory.get()
				.asGlobalAdmin()
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertTrue(response.body().contains("\"isGlobalAdmin\":true"));
	}

	@Test
	void accountInfoDelegatedAdmin() throws Exception {
		final Factory factory = new Factory(Provisioning.getInstance(), "test.com");
		final Account account = factory.get()
				.withAttribute(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, "TRUE")
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertTrue(response.body().contains("\"isGlobalAdmin\":false"));
	}

	@Test
	void accountInfoDomainAdmin() throws Exception {
		final Factory factory = new Factory(Provisioning.getInstance(), "test.com");
		final Account account = factory.get()
				.withAttribute(ZAttrProvisioning.A_zimbraIsDomainAdminAccount, "TRUE")
				.create();

		final Response response = server.getHttpClient().get(
				server.getInternalApiEndpoint() + "/accounts/" + account.getId() + "/info");

		assertTrue(response.body().contains("\"isGlobalAdmin\":false"));
	}

}

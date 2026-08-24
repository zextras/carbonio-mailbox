/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.resource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.TestHttpClient.Response;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Provisioning;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class CosResourceIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	@Test
	void countsTheRequestedCos() throws Exception {
		final Cos cos = createCos();
		createAccountOn(cos);
		createAccountOn(cos);

		final Response response = countCos("?cosId=" + cos.getId());

		assertEquals(200, response.statusCode());
		assertThatJson(response.body())
				.isEqualTo(String.format("{\"total\": 2, \"byCos\": {\"%s\": 2}}", cos.getId()));
	}

	@Test
	void countsEveryCosWhenNoCosIdIsGiven() throws Exception {
		final Cos cos = createCos();
		createAccountOn(cos);

		final Response response = countCos("");

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).node("byCos." + cos.getId()).isEqualTo(1);
	}

	@Test
	void skipsAccountsWithAnExcludedStatus() throws Exception {
		final Cos cos = createCos();
		createAccountOn(cos);
		createClosedAccountOn(cos);

		final Response response = countCos("?cosId=" + cos.getId() + "&excludeAccountStatus=closed");

		assertEquals(200, response.statusCode());
		assertThatJson(response.body())
				.isEqualTo(String.format("{\"total\": 1, \"byCos\": {\"%s\": 1}}", cos.getId()));
	}

	@Test
	void rejectsAnUnknownAccountStatus() throws Exception {
		final Response response = countCos("?excludeAccountStatus=nonesuch");

		assertEquals(400, response.statusCode());
		assertThatJson(response.body()).node("error").asString().contains("nonesuch");
	}

	@Test
	void rejectsMoreThanOneHundredCosIds() throws Exception {
		final String cosIds = IntStream.rangeClosed(1, 101)
				.mapToObj(i -> "cosId=" + UUID.randomUUID())
				.collect(Collectors.joining("&"));

		final Response response = countCos("?" + cosIds);

		assertEquals(400, response.statusCode());
		assertThatJson(response.body()).node("error")
				.isEqualTo("Too many entries: max 100 allowed");
	}

	@Test
	void returnsTheCos() throws Exception {
		final Cos cos = createCos();

		final Response response = getCos(cos.getId());

		assertEquals(200, response.statusCode());
		assertThatJson(response.body())
				.isEqualTo(String.format("{\"id\": \"%s\", \"name\": \"%s\"}", cos.getId(), cos.getName()));
	}

	@Test
	void isNotFoundWhenTheCosDoesNotExist() throws Exception {
		final String unknownCosId = UUID.randomUUID().toString();

		final Response response = getCos(unknownCosId);

		assertEquals(404, response.statusCode());
		assertThatJson(response.body()).node("error").asString().contains(unknownCosId);
	}

	private static Response countCos(String query) throws Exception {
		return server.getHttpClient().get(server.getInternalApiEndpoint() + "/cos/count" + query);
	}

	private static Response getCos(String cosId) throws Exception {
		return server.getHttpClient().get(server.getInternalApiEndpoint() + "/cos/" + cosId);
	}

	private static Cos createCos() throws ServiceException {
		return Provisioning.getInstance().createCos("cos-" + UUID.randomUUID(), new HashMap<>());
	}

	private static void createAccountOn(Cos cos) throws ServiceException {
		server.getAccountFactory()
				.withAttribute(Provisioning.A_zimbraCOSId, cos.getId())
				.create();
	}

	private static void createClosedAccountOn(Cos cos) throws ServiceException {
		server.getAccountFactory()
				.withAttribute(Provisioning.A_zimbraCOSId, cos.getId())
				.withAttribute(Provisioning.A_zimbraAccountStatus, Provisioning.ACCOUNT_STATUS_CLOSED)
				.create();
	}
}

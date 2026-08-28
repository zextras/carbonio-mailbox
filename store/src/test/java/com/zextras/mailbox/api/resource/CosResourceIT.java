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

	private static Response getCos(String cosId) throws Exception {
		return server.getHttpClient().get(server.getInternalApiEndpoint() + "/cos/" + cosId);
	}

	private static Cos createCos() throws ServiceException {
		return Provisioning.getInstance().createCos("cos-" + UUID.randomUUID(), new HashMap<>());
	}
}

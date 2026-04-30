/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.resource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.TestHttpClient.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class MailNotificationResourceIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final String ENDPOINT_PATH = "/accounts/mail/notifications";

	@Test
	void emptyRecipients() throws Exception {
		final String body = json(Map.of(
				"subject", "Your storage is almost full",
				"body", "<html><body>Quota warning</body></html>",
				"recipients", List.of()));

		final Response response = post(body);

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isObject().containsEntry("accepted", 0);
	}

	@Test
	void missingRecipients() throws Exception {
		final Map<String, Object> payload = new HashMap<>();
		payload.put("subject", "Your storage is almost full");
		payload.put("body", "<html><body>Quota warning</body></html>");
		final String body = json(payload);

		final Response response = post(body);

		assertEquals(200, response.statusCode());
		assertThatJson(response.body()).isObject().containsEntry("accepted", 0);
	}

	@Test
	void errorOnMissingSubject() throws Exception {
		final Map<String, Object> payload = new HashMap<>();
		payload.put("body", "<html><body>Quota warning</body></html>");
		payload.put("recipients", List.of());
		final String body = json(payload);

		final Response response = post(body);

		assertEquals(400, response.statusCode());
	}

	@Test
	void errorOnEmptySubject() throws Exception {
		final String body = json(Map.of(
				"subject", "",
				"body", "<html><body>Quota warning</body></html>",
				"recipients", List.of()));

		final Response response = post(body);

		assertEquals(400, response.statusCode());
	}

	@Test
	void errorOnMissingBody() throws Exception {
		final Map<String, Object> payload = new HashMap<>();
		payload.put("subject", "Your storage is almost full");
		payload.put("recipients", List.of());
		final String body = json(payload);

		final Response response = post(body);

		assertEquals(400, response.statusCode());
	}

	@Test
	void errorOnEmptyBody() throws Exception {
		final String body = json(Map.of(
				"subject", "Your storage is almost full",
				"body", "",
				"recipients", List.of()));

		final Response response = post(body);

		assertEquals(400, response.statusCode());
	}

	@Test
	void errorOnEmptyJson() throws Exception {
		final Response response = post("{}");

		assertEquals(400, response.statusCode());
	}

	private Response post(String body) throws Exception {
		return server.getHttpClient().post(
				server.getInternalApiEndpoint() + ENDPOINT_PATH, body);
	}

	private static String json(Map<String, Object> payload) throws Exception {
		return objectMapper.writeValueAsString(payload);
	}
}

/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.TestHttpClient.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class InternalApiServerIT {

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	@Test
	void openApiJsonShouldBeServed() throws Exception {
		final Response response = server.getHttpClient().get(
				"http://localhost:" + server.getInternalApiPort() + "/internal/openapi.json");

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"openapi\""), "Response should contain OpenAPI spec");
	}
}

/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.resource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.util.MailboxServerExtension;
import com.zextras.mailbox.util.TestHttpClient.Response;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class ConfigResourceIT {

	private static final String CLAMAV_URL = ZAttrProvisioning.A_carbonioClamAVDatabaseCustomURL;

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	@Test
	void appliesTheOperations() throws Exception {
		final String oldUrl = signatureUrl();
		final String newUrl = signatureUrl();
		addToConfig(oldUrl);

		final Response response = patchConfig(operations(
				operation("add", newUrl),
				operation("remove", oldUrl)));

		assertEquals(204, response.statusCode());
		assertTrue(configValues().contains(newUrl));
		assertFalse(configValues().contains(oldUrl));
	}

	@Test
	void setReplacesAllValues() throws Exception {
		addToConfig(signatureUrl());
		final String url = signatureUrl();

		final Response response = patchConfig(operations(operation("set", url)));

		assertEquals(204, response.statusCode());
		assertEquals(List.of(url), configValues());
	}

	@Test
	void unsetClearsTheAttribute() throws Exception {
		addToConfig(signatureUrl());

		final Response response = patchConfig(operations(String.format(
				"{\"attribute\": \"%s\", \"op\": \"unset\"}", CLAMAV_URL)));

		assertEquals(204, response.statusCode());
		assertTrue(configValues().isEmpty());
	}

	@Test
	void rejectsAddingToASingleValuedAttribute() throws Exception {
		final Response response = patchConfig(operations(
				"{\"attribute\": \"zimbraMtaSmtpdBanner\", \"op\": \"add\", \"values\": [\"banner\"]}"));

		assertEquals(400, response.statusCode());
		assertThatJson(response.body()).node("error").asString().contains("zimbraMtaSmtpdBanner");
	}

	@Test
	void rejectsAnUnknownAttribute() throws Exception {
		final Response response = patchConfig(operations(
				"{\"attribute\": \"carbonioNoSuchAttribute\", \"op\": \"set\", \"values\": [\"value\"]}"));

		assertEquals(400, response.statusCode());
		assertThatJson(response.body()).node("error").asString().contains("carbonioNoSuchAttribute");
	}

	@Test
	void rejectsAnImmutableAttribute() throws Exception {
		final Response response = patchConfig(operations(
				"{\"attribute\": \"zimbraMtaAuthURL\", \"op\": \"set\", \"values\": [\"http://localhost\"]}"));

		assertEquals(400, response.statusCode());
		assertThatJson(response.body()).node("error").asString().contains("zimbraMtaAuthURL");
	}

	@Test
	void rejectsAnInvalidValue() throws Exception {
		final Response response = patchConfig(operations(
				"{\"attribute\": \"zimbraMtaDnsLookupsEnabled\", \"op\": \"set\", \"values\": [\"maybe\"]}"));

		assertEquals(400, response.statusCode());
		assertThatJson(response.body()).node("error").asString().contains("zimbraMtaDnsLookupsEnabled");
	}

	@Test
	void rejectsAnUnknownOperation() throws Exception {
		final Response response = patchConfig(operations(operation("replace", signatureUrl())));

		assertEquals(400, response.statusCode());
		assertThatJson(response.body()).node("error").asString().contains("replace");
	}

	@Test
	void rejectsAnEmptyRequest() throws Exception {
		final Response response = patchConfig("{}");

		assertEquals(400, response.statusCode());
	}

	private static Response patchConfig(String body) throws Exception {
		return server.getHttpClient().patch(server.getInternalApiEndpoint() + "/config", body);
	}

	private static String operations(String... operations) {
		return "{\"operations\": [" + String.join(", ", operations) + "]}";
	}

	private static String operation(String op, String url) {
		return String.format("{\"attribute\": \"%s\", \"op\": \"%s\", \"values\": [\"%s\"]}", CLAMAV_URL, op, url);
	}

	private static String signatureUrl() {
		return "http://avdb.local/av_signatures/" + UUID.randomUUID() + ".ndb";
	}

	private static void addToConfig(String url) throws ServiceException {
		final Provisioning provisioning = Provisioning.getInstance();
		provisioning.modifyAttrs(provisioning.getConfig(), Map.of("+" + CLAMAV_URL, url));
	}

	private static List<String> configValues() throws ServiceException {
		return List.of(Provisioning.getInstance().getConfig().getMultiAttr(CLAMAV_URL));
	}
}

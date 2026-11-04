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
	void addsAndRemovesValues() throws Exception {
		final String oldUrl = signatureUrl();
		final String newUrl = signatureUrl();
		addToConfig(oldUrl);

		final Response response = patchConfig(String.format(
				"{\"add\": {\"%1$s\": [\"%2$s\"]}, \"remove\": {\"%1$s\": [\"%3$s\"]}}", CLAMAV_URL, newUrl, oldUrl));

		assertEquals(204, response.statusCode());
		final List<String> urls = configValues();
		assertTrue(urls.contains(newUrl));
		assertFalse(urls.contains(oldUrl));
	}

	@Test
	void rejectsAnAttributeThatIsNotAllowed() throws Exception {
		final Response response = patchConfig(
				"{\"add\": {\"zimbraMtaMyNetworks\": [\"0.0.0.0/0\"]}}");

		assertEquals(400, response.statusCode());
		assertThatJson(response.body()).node("error").asString().contains("zimbraMtaMyNetworks");
	}

	@Test
	void rejectsAnEmptyRequest() throws Exception {
		final Response response = patchConfig("{}");

		assertEquals(400, response.statusCode());
	}

	private static Response patchConfig(String body) throws Exception {
		return server.getHttpClient().patch(server.getInternalApiEndpoint() + "/config", body);
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

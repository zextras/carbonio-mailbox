/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.util.MailboxServerExtension;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import io.vavr.control.Try;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Tag("e2e")
class ConfigServiceIT {

	private static final String CLAMAV_URL = ZAttrProvisioning.A_carbonioClamAVDatabaseCustomURL;

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private final ConfigService configService = new ConfigService(Provisioning::getInstance, Set.of(CLAMAV_URL));

	@Test
	void addsSeveralValues() throws Exception {
		final String first = signatureUrl();
		final String second = signatureUrl();

		final Try<Void> result = configService.modifyConfig(Map.of(CLAMAV_URL, List.of(first, second)), null);

		assertTrue(result.isSuccess());
		assertTrue(configValues().containsAll(List.of(first, second)));
	}

	@Test
	void keepsTheOtherValues() throws Exception {
		final String kept = signatureUrl();
		final String removed = signatureUrl();
		addToConfig(kept, removed);

		final Try<Void> result = configService.modifyConfig(null, Map.of(CLAMAV_URL, List.of(removed)));

		assertTrue(result.isSuccess());
		assertTrue(configValues().contains(kept));
		assertFalse(configValues().contains(removed));
	}

	@Test
	void ignoresAValueAlreadyPresent() throws Exception {
		final String url = signatureUrl();
		addToConfig(url);

		final Try<Void> result = configService.modifyConfig(Map.of(CLAMAV_URL, List.of(url)), null);

		assertTrue(result.isSuccess());
		assertEquals(1, configValues().stream().filter(url::equals).count());
	}

	@Test
	void ignoresTheRemovalOfAMissingValue() throws Exception {
		final String url = signatureUrl();

		final Try<Void> result = configService.modifyConfig(null, Map.of(CLAMAV_URL, List.of(url)));

		assertTrue(result.isSuccess());
		assertFalse(configValues().contains(url));
	}

	@Test
	void rejectsAnAttributeThatIsNotAllowed() throws Exception {
		final List<String> before = configValues();

		final Try<Void> result = configService.modifyConfig(
				Map.of(CLAMAV_URL, List.of(signatureUrl()), "zimbraMtaMyNetworks", List.of("0.0.0.0/0")), null);

		assertInvalid(result);
		assertEquals(before, configValues());
	}

	@Test
	void rejectsTheSameValueAddedAndRemoved() {
		final String url = signatureUrl();

		final Try<Void> result = configService.modifyConfig(
				Map.of(CLAMAV_URL, List.of(url)), Map.of(CLAMAV_URL, List.of(url)));

		assertInvalid(result);
	}

	@Test
	void rejectsABlankValue() {
		final Try<Void> result = configService.modifyConfig(Map.of(CLAMAV_URL, List.of(" ")), null);

		assertInvalid(result);
	}

	@Test
	void rejectsAnEmptyListOfValues() {
		final Try<Void> result = configService.modifyConfig(null, Map.of(CLAMAV_URL, List.of()));

		assertInvalid(result);
	}

	@Test
	void rejectsNothingToModify() {
		final Try<Void> result = configService.modifyConfig(Map.of(), null);

		assertInvalid(result);
	}

	private static void assertInvalid(Try<Void> result) {
		assertTrue(result.isFailure());
		assertInstanceOf(InvalidConfigModificationException.class, result.getCause());
	}

	private static String signatureUrl() {
		return "http://avdb.local/av_signatures/" + UUID.randomUUID() + ".ndb";
	}

	private static void addToConfig(String... urls) throws ServiceException {
		final Provisioning provisioning = Provisioning.getInstance();
		provisioning.modifyAttrs(provisioning.getConfig(), Map.of("+" + CLAMAV_URL, urls));
	}

	private static List<String> configValues() throws ServiceException {
		return Arrays.asList(Provisioning.getInstance().getConfig().getMultiAttr(CLAMAV_URL));
	}
}

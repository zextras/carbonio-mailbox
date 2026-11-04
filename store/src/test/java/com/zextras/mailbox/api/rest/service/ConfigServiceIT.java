/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zextras.mailbox.util.MailboxServerExtension;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.StoreAttributeManager;
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
	private static final String SMTPD_BANNER = ZAttrProvisioning.A_zimbraMtaSmtpdBanner;

	@RegisterExtension
	static final MailboxServerExtension server = new MailboxServerExtension();

	private final ConfigService configService = new ConfigService(
			Provisioning::getInstance, ConfigServiceIT::attributeManager, Set.of(CLAMAV_URL, SMTPD_BANNER));

	@Test
	void addsSeveralValues() throws Exception {
		final String first = signatureUrl();
		final String second = signatureUrl();

		final Try<Void> result = configService.modifyConfig(add(CLAMAV_URL, first, second));

		assertTrue(result.isSuccess());
		assertTrue(clamavUrls().containsAll(List.of(first, second)));
	}

	@Test
	void addsAndRemovesValuesOfTheSameAttribute() throws Exception {
		final String removed = signatureUrl();
		final String added = signatureUrl();
		addToConfig(removed);

		final Try<Void> result = configService.modifyConfig(
				new ConfigChanges(null, null, Map.of(CLAMAV_URL, List.of(added)), Map.of(CLAMAV_URL, List.of(removed))));

		assertTrue(result.isSuccess());
		assertTrue(clamavUrls().contains(added));
		assertFalse(clamavUrls().contains(removed));
	}

	@Test
	void keepsTheOtherValues() throws Exception {
		final String kept = signatureUrl();
		final String removed = signatureUrl();
		addToConfig(kept, removed);

		final Try<Void> result = configService.modifyConfig(remove(CLAMAV_URL, removed));

		assertTrue(result.isSuccess());
		assertTrue(clamavUrls().contains(kept));
		assertFalse(clamavUrls().contains(removed));
	}

	@Test
	void ignoresAValueAlreadyPresent() throws Exception {
		final String url = signatureUrl();
		addToConfig(url);

		final Try<Void> result = configService.modifyConfig(add(CLAMAV_URL, url));

		assertTrue(result.isSuccess());
		assertEquals(1, clamavUrls().stream().filter(url::equals).count());
	}

	@Test
	void ignoresTheRemovalOfAMissingValue() throws Exception {
		final String url = signatureUrl();

		final Try<Void> result = configService.modifyConfig(remove(CLAMAV_URL, url));

		assertTrue(result.isSuccess());
		assertFalse(clamavUrls().contains(url));
	}

	@Test
	void setReplacesAllValuesOfAMultiValuedAttribute() throws Exception {
		addToConfig(signatureUrl());
		final String first = signatureUrl();
		final String second = signatureUrl();

		final Try<Void> result = configService.modifyConfig(set(CLAMAV_URL, first, second));

		assertTrue(result.isSuccess());
		assertEquals(Set.of(first, second), Set.copyOf(clamavUrls()));
	}

	@Test
	void setsASingleValuedAttribute() throws Exception {
		final String banner = "banner " + UUID.randomUUID();

		final Try<Void> result = configService.modifyConfig(set(SMTPD_BANNER, banner));

		assertTrue(result.isSuccess());
		assertEquals(banner, Provisioning.getInstance().getConfig().getAttr(SMTPD_BANNER));
	}

	@Test
	void unsetsAnAttribute() throws Exception {
		configService.modifyConfig(set(SMTPD_BANNER, "banner")).get();

		final Try<Void> result = configService.modifyConfig(unset(SMTPD_BANNER));

		assertTrue(result.isSuccess());
		assertNull(Provisioning.getInstance().getConfig().getAttr(SMTPD_BANNER));
	}

	@Test
	void unsetsAllValuesOfAMultiValuedAttribute() throws Exception {
		addToConfig(signatureUrl(), signatureUrl());

		final Try<Void> result = configService.modifyConfig(unset(CLAMAV_URL));

		assertTrue(result.isSuccess());
		assertTrue(clamavUrls().isEmpty());
	}

	@Test
	void rejectsSeveralValuesForASingleValuedAttribute() {
		final Try<Void> result = configService.modifyConfig(set(SMTPD_BANNER, "first", "second"));

		assertInvalid(result);
	}

	@Test
	void rejectsAddingToASingleValuedAttribute() {
		final Try<Void> result = configService.modifyConfig(add(SMTPD_BANNER, "banner"));

		assertInvalid(result);
	}

	@Test
	void rejectsRemovingFromASingleValuedAttribute() {
		final Try<Void> result = configService.modifyConfig(remove(SMTPD_BANNER, "banner"));

		assertInvalid(result);
	}

	@Test
	void rejectsAnAttributeSetAndChangedByValue() {
		final Try<Void> result = configService.modifyConfig(new ConfigChanges(
				Map.of(CLAMAV_URL, List.of(signatureUrl())), null, Map.of(CLAMAV_URL, List.of(signatureUrl())), null));

		assertInvalid(result);
	}

	@Test
	void rejectsAnAttributeSetAndUnset() {
		final Try<Void> result = configService.modifyConfig(new ConfigChanges(
				Map.of(SMTPD_BANNER, List.of("banner")), List.of(SMTPD_BANNER), null, null));

		assertInvalid(result);
	}

	@Test
	void rejectsAnAttributeThatIsNotAllowed() throws Exception {
		final List<String> before = clamavUrls();

		final Try<Void> result = configService.modifyConfig(new ConfigChanges(
				null, null, Map.of(CLAMAV_URL, List.of(signatureUrl()), "zimbraMtaMyNetworks", List.of("0.0.0.0/0")), null));

		assertInvalid(result);
		assertEquals(before, clamavUrls());
	}

	@Test
	void rejectsUnsettingAnAttributeThatIsNotAllowed() {
		final Try<Void> result = configService.modifyConfig(unset("zimbraMtaMyNetworks"));

		assertInvalid(result);
	}

	@Test
	void rejectsTheSameValueAddedAndRemoved() {
		final String url = signatureUrl();

		final Try<Void> result = configService.modifyConfig(
				new ConfigChanges(null, null, Map.of(CLAMAV_URL, List.of(url)), Map.of(CLAMAV_URL, List.of(url))));

		assertInvalid(result);
	}

	@Test
	void rejectsABlankValue() {
		final Try<Void> result = configService.modifyConfig(set(SMTPD_BANNER, " "));

		assertInvalid(result);
	}

	@Test
	void rejectsAnEmptyListOfValues() {
		final Try<Void> result = configService.modifyConfig(remove(CLAMAV_URL));

		assertInvalid(result);
	}

	@Test
	void rejectsNothingToModify() {
		final Try<Void> result = configService.modifyConfig(new ConfigChanges(Map.of(), List.of(), null, null));

		assertInvalid(result);
	}

	private static ConfigChanges set(String attribute, String... values) {
		return new ConfigChanges(Map.of(attribute, List.of(values)), null, null, null);
	}

	private static ConfigChanges unset(String attribute) {
		return new ConfigChanges(null, List.of(attribute), null, null);
	}

	private static ConfigChanges add(String attribute, String... values) {
		return new ConfigChanges(null, null, Map.of(attribute, List.of(values)), null);
	}

	private static ConfigChanges remove(String attribute, String... values) {
		return new ConfigChanges(null, null, null, Map.of(attribute, List.of(values)));
	}

	private static void assertInvalid(Try<Void> result) {
		assertTrue(result.isFailure());
		assertInstanceOf(InvalidConfigModificationException.class, result.getCause());
	}

	private static AttributeManager attributeManager() {
		try {
			return StoreAttributeManager.getInstance();
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	}

	private static String signatureUrl() {
		return "http://avdb.local/av_signatures/" + UUID.randomUUID() + ".ndb";
	}

	private static void addToConfig(String... urls) throws ServiceException {
		final Provisioning provisioning = Provisioning.getInstance();
		provisioning.modifyAttrs(provisioning.getConfig(), Map.of("+" + CLAMAV_URL, urls));
	}

	private static List<String> clamavUrls() throws ServiceException {
		return Arrays.asList(Provisioning.getInstance().getConfig().getMultiAttr(CLAMAV_URL));
	}
}

/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.service;

import static com.zextras.mailbox.api.rest.service.ConfigOperation.add;
import static com.zextras.mailbox.api.rest.service.ConfigOperation.remove;
import static com.zextras.mailbox.api.rest.service.ConfigOperation.set;
import static com.zextras.mailbox.api.rest.service.ConfigOperation.unset;
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
import java.util.Collections;
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

		final Try<Void> result = modify(add(CLAMAV_URL, first, second));

		assertTrue(result.isSuccess());
		assertTrue(clamavUrls().containsAll(List.of(first, second)));
	}

	@Test
	void addsAndRemovesValuesOfTheSameAttribute() throws Exception {
		final String removed = signatureUrl();
		final String added = signatureUrl();
		addToConfig(removed);

		final Try<Void> result = modify(add(CLAMAV_URL, added), remove(CLAMAV_URL, removed));

		assertTrue(result.isSuccess());
		assertTrue(clamavUrls().contains(added));
		assertFalse(clamavUrls().contains(removed));
	}

	@Test
	void keepsTheOtherValues() throws Exception {
		final String kept = signatureUrl();
		final String removed = signatureUrl();
		addToConfig(kept, removed);

		final Try<Void> result = modify(remove(CLAMAV_URL, removed));

		assertTrue(result.isSuccess());
		assertTrue(clamavUrls().contains(kept));
		assertFalse(clamavUrls().contains(removed));
	}

	@Test
	void ignoresAValueAlreadyPresent() throws Exception {
		final String url = signatureUrl();
		addToConfig(url);

		final Try<Void> result = modify(add(CLAMAV_URL, url));

		assertTrue(result.isSuccess());
		assertEquals(1, Collections.frequency(clamavUrls(), url));
	}

	@Test
	void ignoresTheRemovalOfAMissingValue() throws Exception {
		final String url = signatureUrl();

		final Try<Void> result = modify(remove(CLAMAV_URL, url));

		assertTrue(result.isSuccess());
		assertFalse(clamavUrls().contains(url));
	}

	@Test
	void setReplacesAllValuesOfAMultiValuedAttribute() throws Exception {
		addToConfig(signatureUrl());
		final String first = signatureUrl();
		final String second = signatureUrl();

		final Try<Void> result = modify(set(CLAMAV_URL, first, second));

		assertTrue(result.isSuccess());
		assertEquals(Set.of(first, second), Set.copyOf(clamavUrls()));
	}

	@Test
	void setsASingleValuedAttribute() throws Exception {
		final String banner = "banner " + UUID.randomUUID();

		final Try<Void> result = modify(set(SMTPD_BANNER, banner));

		assertTrue(result.isSuccess());
		assertEquals(banner, smtpdBanner());
	}

	@Test
	void unsetsAnAttribute() throws Exception {
		modify(set(SMTPD_BANNER, "banner")).get();

		final Try<Void> result = modify(unset(SMTPD_BANNER));

		assertTrue(result.isSuccess());
		assertNull(smtpdBanner());
	}

	@Test
	void unsetsAllValuesOfAMultiValuedAttribute() throws Exception {
		addToConfig(signatureUrl(), signatureUrl());

		final Try<Void> result = modify(unset(CLAMAV_URL));

		assertTrue(result.isSuccess());
		assertTrue(clamavUrls().isEmpty());
	}

	@Test
	void theLastSetWins() throws Exception {
		final Try<Void> result = modify(set(SMTPD_BANNER, "first"), set(SMTPD_BANNER, "last"));

		assertTrue(result.isSuccess());
		assertEquals("last", smtpdBanner());
	}

	@Test
	void addAfterSetExtendsTheReplacement() throws Exception {
		addToConfig(signatureUrl());
		final String first = signatureUrl();
		final String second = signatureUrl();

		final Try<Void> result = modify(set(CLAMAV_URL, first), add(CLAMAV_URL, second));

		assertTrue(result.isSuccess());
		assertEquals(Set.of(first, second), Set.copyOf(clamavUrls()));
	}

	@Test
	void removeAfterAddRemovesAValueAlreadyPresent() throws Exception {
		final String url = signatureUrl();
		addToConfig(url);

		final Try<Void> result = modify(add(CLAMAV_URL, url), remove(CLAMAV_URL, url));

		assertTrue(result.isSuccess());
		assertFalse(clamavUrls().contains(url));
	}

	@Test
	void addAfterRemoveKeepsTheValue() throws Exception {
		final String url = signatureUrl();
		addToConfig(url);

		final Try<Void> result = modify(remove(CLAMAV_URL, url), add(CLAMAV_URL, url));

		assertTrue(result.isSuccess());
		assertTrue(clamavUrls().contains(url));
	}

	@Test
	void addAfterUnsetLeavesOnlyTheAddedValue() throws Exception {
		addToConfig(signatureUrl());
		final String url = signatureUrl();

		final Try<Void> result = modify(unset(CLAMAV_URL), add(CLAMAV_URL, url));

		assertTrue(result.isSuccess());
		assertEquals(List.of(url), clamavUrls());
	}

	@Test
	void matchesTheAttributeIgnoringCase() throws Exception {
		final String url = signatureUrl();

		final Try<Void> result = modify(add(CLAMAV_URL.toUpperCase(), url));

		assertTrue(result.isSuccess());
		assertTrue(clamavUrls().contains(url));
	}

	@Test
	void rejectsSeveralValuesForASingleValuedAttribute() {
		assertInvalid(modify(set(SMTPD_BANNER, "first", "second")));
	}

	@Test
	void rejectsAddingToASingleValuedAttribute() {
		assertInvalid(modify(add(SMTPD_BANNER, "banner")));
	}

	@Test
	void rejectsRemovingFromASingleValuedAttribute() {
		assertInvalid(modify(remove(SMTPD_BANNER, "banner")));
	}

	@Test
	void rejectsTheWholeRequestWhenAnAttributeIsNotAllowed() throws Exception {
		final List<String> before = clamavUrls();

		final Try<Void> result = modify(add(CLAMAV_URL, signatureUrl()), add("zimbraMtaMyNetworks", "0.0.0.0/0"));

		assertInvalid(result);
		assertEquals(before, clamavUrls());
	}

	@Test
	void rejectsUnsettingAnAttributeThatIsNotAllowed() {
		assertInvalid(modify(unset("zimbraMtaMyNetworks")));
	}

	@Test
	void rejectsAnUnknownOperation() {
		assertInvalid(modify(new ConfigOperation(CLAMAV_URL, "replace", List.of(signatureUrl()))));
	}

	@Test
	void rejectsAMissingAttribute() {
		assertInvalid(modify(new ConfigOperation(null, "add", List.of(signatureUrl()))));
	}

	@Test
	void rejectsValuesForUnset() {
		assertInvalid(modify(new ConfigOperation(CLAMAV_URL, "unset", List.of(signatureUrl()))));
	}

	@Test
	void rejectsABlankValue() {
		assertInvalid(modify(set(SMTPD_BANNER, " ")));
	}

	@Test
	void rejectsAnEmptyListOfValues() {
		assertInvalid(modify(remove(CLAMAV_URL)));
	}

	@Test
	void rejectsNothingToModify() {
		assertInvalid(configService.modifyConfig(List.of()));
	}

	private Try<Void> modify(ConfigOperation... operations) {
		return configService.modifyConfig(List.of(operations));
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

	private static String smtpdBanner() throws ServiceException {
		return Provisioning.getInstance().getConfig().getAttr(SMTPD_BANNER);
	}
}

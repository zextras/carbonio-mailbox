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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ConfigModificationsTest {

	private static final String ATTRIBUTE = "attr";

	@Test
	void setReplacesTheValues() {
		assertEquals(Map.of(ATTRIBUTE, List.of("a", "b")), modificationsOf(set(ATTRIBUTE, "a", "b")));
	}

	@Test
	void unsetClearsTheAttribute() {
		assertEquals(Map.of(ATTRIBUTE, ""), modificationsOf(unset(ATTRIBUTE)));
	}

	@Test
	void addAndRemoveBecomePrefixedModifications() {
		assertEquals(Map.of("+" + ATTRIBUTE, List.of("a"), "-" + ATTRIBUTE, List.of("b")),
				modificationsOf(add(ATTRIBUTE, "a"), remove(ATTRIBUTE, "b")));
	}

	@Test
	void theLastSetWins() {
		assertEquals(Map.of(ATTRIBUTE, List.of("b")), modificationsOf(set(ATTRIBUTE, "a"), set(ATTRIBUTE, "b")));
	}

	@Test
	void setDiscardsTheEarlierAddAndRemove() {
		assertEquals(Map.of(ATTRIBUTE, List.of("c")),
				modificationsOf(add(ATTRIBUTE, "a"), remove(ATTRIBUTE, "b"), set(ATTRIBUTE, "c")));
	}

	@Test
	void addAfterSetExtendsTheValues() {
		assertEquals(Map.of(ATTRIBUTE, List.of("a", "b")), modificationsOf(set(ATTRIBUTE, "a"), add(ATTRIBUTE, "b")));
	}

	@Test
	void removeAfterSetShrinksTheValues() {
		assertEquals(Map.of(ATTRIBUTE, List.of("a")), modificationsOf(set(ATTRIBUTE, "a", "b"), remove(ATTRIBUTE, "b")));
	}

	@Test
	void addAfterUnsetLeavesOnlyTheAddedValue() {
		assertEquals(Map.of(ATTRIBUTE, List.of("a")), modificationsOf(unset(ATTRIBUTE), add(ATTRIBUTE, "a")));
	}

	@Test
	void removeAfterUnsetKeepsTheAttributeCleared() {
		assertEquals(Map.of(ATTRIBUTE, ""), modificationsOf(unset(ATTRIBUTE), remove(ATTRIBUTE, "a")));
	}

	@Test
	void removeAfterAddRemovesTheValue() {
		assertEquals(Map.of("-" + ATTRIBUTE, List.of("a")), modificationsOf(add(ATTRIBUTE, "a"), remove(ATTRIBUTE, "a")));
	}

	@Test
	void addAfterRemoveAddsTheValue() {
		assertEquals(Map.of("+" + ATTRIBUTE, List.of("a")), modificationsOf(remove(ATTRIBUTE, "a"), add(ATTRIBUTE, "a")));
	}

	@Test
	void keepsAttributesApart() {
		assertEquals(Map.of("+first", List.of("a"), "second", List.of("b")),
				modificationsOf(add("first", "a"), set("second", "b")));
	}

	private static Map<String, Object> modificationsOf(ConfigOperation... operations) {
		return ConfigModifications.of(List.of(operations)).entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey,
						entry -> entry.getValue() instanceof String[] values ? List.of(values) : entry.getValue()));
	}
}

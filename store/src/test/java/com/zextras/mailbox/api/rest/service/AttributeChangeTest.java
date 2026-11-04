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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class AttributeChangeTest {

	private static final String ATTRIBUTE = "attr";

	@Test
	void theLastSetWins() {
		assertEquals(new AttributeChange.Replace(Set.of("b")), changeOf(set(ATTRIBUTE, "a"), set(ATTRIBUTE, "b")));
	}

	@Test
	void addAfterSetExtendsTheReplacement() {
		assertEquals(new AttributeChange.Replace(Set.of("a", "b")), changeOf(set(ATTRIBUTE, "a"), add(ATTRIBUTE, "b")));
	}

	@Test
	void removeAfterSetShrinksTheReplacement() {
		assertEquals(new AttributeChange.Replace(Set.of("a")), changeOf(set(ATTRIBUTE, "a", "b"), remove(ATTRIBUTE, "b")));
	}

	@Test
	void addAfterUnsetLeavesOnlyTheAddedValue() {
		assertEquals(new AttributeChange.Replace(Set.of("a")), changeOf(unset(ATTRIBUTE), add(ATTRIBUTE, "a")));
	}

	@Test
	void removeAfterAddRemovesTheValue() {
		assertEquals(new AttributeChange.Delta(Set.of(), Set.of("a")), changeOf(add(ATTRIBUTE, "a"), remove(ATTRIBUTE, "a")));
	}

	@Test
	void addAfterRemoveAddsTheValue() {
		assertEquals(new AttributeChange.Delta(Set.of("a"), Set.of()), changeOf(remove(ATTRIBUTE, "a"), add(ATTRIBUTE, "a")));
	}

	@Test
	void addsAndRemovesDifferentValues() {
		assertEquals(new AttributeChange.Delta(Set.of("a"), Set.of("b")), changeOf(add(ATTRIBUTE, "a"), remove(ATTRIBUTE, "b")));
	}

	@Test
	void replaceBecomesAPlainModification() {
		final Map<String, Object> modifications = changeOf(set(ATTRIBUTE, "a")).modifications(ATTRIBUTE);

		assertEquals(Set.of(ATTRIBUTE), modifications.keySet());
		assertArrayEquals(new String[] {"a"}, (String[]) modifications.get(ATTRIBUTE));
	}

	@Test
	void unsetBecomesAnEmptyValue() {
		assertEquals(Map.of(ATTRIBUTE, ""), changeOf(unset(ATTRIBUTE)).modifications(ATTRIBUTE));
	}

	@Test
	void deltaBecomesPrefixedModifications() {
		final Map<String, Object> modifications = changeOf(add(ATTRIBUTE, "a"), remove(ATTRIBUTE, "b")).modifications(ATTRIBUTE);

		assertEquals(Set.of("+" + ATTRIBUTE, "-" + ATTRIBUTE), modifications.keySet());
		assertArrayEquals(new String[] {"a"}, (String[]) modifications.get("+" + ATTRIBUTE));
		assertArrayEquals(new String[] {"b"}, (String[]) modifications.get("-" + ATTRIBUTE));
	}

	@Test
	void emptyDeltaHasNoModifications() {
		assertEquals(Map.of(), new AttributeChange.Delta(Set.of(), Set.of()).modifications(ATTRIBUTE));
	}

	private static AttributeChange changeOf(ConfigOperation... operations) {
		return Stream.of(operations).map(AttributeChange::of).reduce(AttributeChange::then).orElseThrow();
	}
}

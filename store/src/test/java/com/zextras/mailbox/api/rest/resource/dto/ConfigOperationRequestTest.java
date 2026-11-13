/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.api.rest.resource.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zextras.mailbox.api.rest.service.ConfigOperation;
import com.zextras.mailbox.api.rest.service.InvalidConfigModificationException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigOperationRequestTest {

	private static final String ATTRIBUTE = "carbonioClamAVDatabaseCustomURL";
	private static final List<String> VALUES = List.of("http://avdb/a.ndb");

	@Test
	void convertsEachOperation() {
		assertEquals(new ConfigOperation.Set(ATTRIBUTE, VALUES), request("set", VALUES).toOperation());
		assertEquals(new ConfigOperation.Unset(ATTRIBUTE), request("unset", null).toOperation());
		assertEquals(new ConfigOperation.Add(ATTRIBUTE, VALUES), request("add", VALUES).toOperation());
		assertEquals(new ConfigOperation.Remove(ATTRIBUTE, VALUES), request("remove", VALUES).toOperation());
	}

	@Test
	void rejectsAnUnknownOperation() {
		assertInvalid(request("replace", VALUES));
	}

	@Test
	void rejectsAMissingOperation() {
		assertInvalid(request(null, VALUES));
	}

	@Test
	void rejectsAMissingAttribute() {
		assertInvalid(new ConfigOperationRequest(null, "add", VALUES));
	}

	@Test
	void rejectsValuesForUnset() {
		assertInvalid(request("unset", VALUES));
	}

	@Test
	void rejectsMissingValues() {
		assertInvalid(request("add", null));
	}

	@Test
	void rejectsAnEmptyListOfValues() {
		assertInvalid(request("remove", List.of()));
	}

	@Test
	void rejectsABlankValue() {
		assertInvalid(request("set", List.of(" ")));
	}

	@Test
	void rejectsANullValue() {
		assertInvalid(request("set", Arrays.asList("http://avdb/a.ndb", null)));
	}

	private static ConfigOperationRequest request(String op, List<String> values) {
		return new ConfigOperationRequest(ATTRIBUTE, op, values);
	}

	private static void assertInvalid(ConfigOperationRequest request) {
		assertThrows(InvalidConfigModificationException.class, request::toOperation);
	}
}

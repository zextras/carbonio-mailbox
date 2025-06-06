/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.redis.testcontainers.RedisContainer;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.ephemeral.EphemeralInput;
import com.zimbra.cs.ephemeral.EphemeralKey;
import com.zimbra.cs.ephemeral.EphemeralResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

class SSDBEphemeralStoreTest {

	@Container
	static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

	@BeforeAll
	static void setUp() {
		redisContainer.start();
	}
	@AfterAll
	static void tearDown() {
		redisContainer.stop();
	}

	private static EphemeralInput[] generateInput() {
		return new EphemeralInput[]{
				new EphemeralInput(new EphemeralKey("testString"), "value1"),
				new EphemeralInput(new EphemeralKey("testBoolean"), true),
				new EphemeralInput(new EphemeralKey("testInteger"), 10),
				new EphemeralInput(new EphemeralKey("testLong"), 10L)
		};
	}

	@ParameterizedTest
	@MethodSource ("generateInput")
	void get(EphemeralInput input) throws ServiceException {
		final SSDBEphemeralStore ssdbEphemeralStore = SSDBEphemeralStore.create(
				redisContainer.getRedisURI());
		final TestLocation testLocation = new TestLocation(new String[]{"ccc"});
		ssdbEphemeralStore.set(input, testLocation);

		final EphemeralResult ephemeralResult = ssdbEphemeralStore.get(input.getEphemeralKey(),
				testLocation);
		Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());
	}

	@Test
	void shouldNotOverrideSameKey_WhenLocationIsDifferent() throws ServiceException {
		final SSDBEphemeralStore store = SSDBEphemeralStore.create(
				redisContainer.getRedisURI());
		final EphemeralKey key = new EphemeralKey("test");
		final TestLocation location1 = new TestLocation(new String[]{"1"});
		final TestLocation location2 = new TestLocation(new String[]{"2"});
		final EphemeralInput input1 = new EphemeralInput(key, "value1");
		final EphemeralInput input2 = new EphemeralInput(key, "value2");
		store.set(input1, location1);
		store.set(input2, location2);

		final EphemeralResult result1 = store.get(key, location1);
		final EphemeralResult result2 = store.get(key, location2);
		Assertions.assertNotEquals(result1.getValue(), result2.getValue());
	}

	@Test
	void set() {
		final SSDBEphemeralStore ssdbEphemeralStore = SSDBEphemeralStore.create(
				redisContainer.getRedisURI());
		final TestLocation testLocation = new TestLocation(new String[]{"ccc"});
		final EphemeralInput input = new EphemeralInput(new EphemeralKey("test"), "aaa");

		Assertions.assertDoesNotThrow(() -> ssdbEphemeralStore.set(input, testLocation));
	}
}
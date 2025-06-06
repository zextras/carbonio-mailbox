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
				new EphemeralInput(new EphemeralKey("test1"), "value1"),
				new EphemeralInput(new EphemeralKey("test2"), "value2"),
				new EphemeralInput(new EphemeralKey("test3"), "value3")
		};
	}

	@ParameterizedTest
	@MethodSource ("generateInput")
	void getString(EphemeralInput input) throws ServiceException {
		final SSDBEphemeralStore ssdbEphemeralStore = SSDBEphemeralStore.create(
				redisContainer.getRedisURI());
		final TestLocation testLocation = new TestLocation(new String[]{"ccc"});
		ssdbEphemeralStore.set(input, testLocation);

		final EphemeralResult ephemeralResult = ssdbEphemeralStore.get(input.getEphemeralKey(),
				testLocation);
		Assertions.assertEquals(input.getValue(), ephemeralResult.getValue());
	}

	@Test
	void getBoolean() throws ServiceException {
		final SSDBEphemeralStore ssdbEphemeralStore = SSDBEphemeralStore.create(
				redisContainer.getRedisURI());
		final TestLocation testLocation = new TestLocation(new String[]{"ccc"});
		final EphemeralKey key = new EphemeralKey("test");
		final Boolean value = true;
		final EphemeralInput input = new EphemeralInput(key, value);
		ssdbEphemeralStore.set(input, testLocation);

		final EphemeralResult ephemeralResult = ssdbEphemeralStore.get(key,
				testLocation);
		Assertions.assertEquals(value.toString(), ephemeralResult.getValue());
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
/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.redis.testcontainers.RedisContainer;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.ephemeral.EphemeralInput;
import com.zimbra.cs.ephemeral.EphemeralInput.AbsoluteExpiration;
import com.zimbra.cs.ephemeral.EphemeralInput.Expiration;
import com.zimbra.cs.ephemeral.EphemeralKey;
import com.zimbra.cs.ephemeral.EphemeralResult;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;

class SSDBEphemeralStoreTest {

  @Container
  static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

  static Jedis jedisClient;

  @BeforeAll
  static void setUp() {
    redisContainer.start();
    jedisClient = new Jedis("redis://localhost:" + redisContainer.getRedisPort());
  }

  @AfterAll
  static void tearDown() {
    redisContainer.stop();
  }

  private static EphemeralInput[] generateInput() {
    return new EphemeralInput[] {
      new EphemeralInput(new EphemeralKey("testString"), "value1"),
      new EphemeralInput(new EphemeralKey("testBoolean"), true),
      new EphemeralInput(new EphemeralKey("testInteger"), 10),
      new EphemeralInput(new EphemeralKey("testLong"), 10L),
    };
  }

  @BeforeEach
  void beforeEach() {
    jedisClient.flushAll();
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void get(EphemeralInput input) throws ServiceException {
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    ssdbEphemeralStore.set(input, testLocation);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), testLocation);
    Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());
  }

  @Test
  void set_nullValueShouldNotStoreAnything() throws ServiceException {
    String k = null;
    @SuppressWarnings("ConstantValue")
    EphemeralInput input = new EphemeralInput(new EphemeralKey(UUID.randomUUID().toString()), k);
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    ssdbEphemeralStore.set(input, testLocation);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), testLocation);
    Assertions.assertNull(ephemeralResult.getValue());
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void set_shouldStoreExpiration_WhenPresent(EphemeralInput input) throws ServiceException {
    input.setExpiration(new AbsoluteExpiration(100L));
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    ssdbEphemeralStore.set(input, testLocation);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), testLocation);
    Assertions.assertEquals(input.getValue() + "|100", ephemeralResult.getValue());
  }

  @Test
  void set_shouldNotStoreKeysWithNegativeRelativeExpiration() {
    final MockExpiration expiresNow = new MockExpiration(-1000);
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("testString"), "value1", expiresNow);
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});

    final ServiceException serviceException = Assertions.assertThrows(ServiceException.class,
        () -> ssdbEphemeralStore.set(input, testLocation));

    Assertions.assertEquals("system failure: Cannot store a key with expiration -1 seconds", serviceException.getMessage());
  }

  @Test
  void set_shouldNotStoreKeysWithZeroRelativeExpiration() {
    final MockExpiration expiresNow = new MockExpiration(0);
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("testString"), "value1", expiresNow);
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});

    final ServiceException serviceException = Assertions.assertThrows(ServiceException.class,
        () -> ssdbEphemeralStore.set(input, testLocation));

    Assertions.assertEquals("system failure: Cannot store a key with expiration 0 seconds", serviceException.getMessage());
  }

  private static class MockExpiration extends Expiration {

    private final int timeToWaitForExpiration;

		private MockExpiration(int timeToWaitForExpiration) {
			this.timeToWaitForExpiration = timeToWaitForExpiration;
		}

		@Override
    public long getMillis() {
      return System.currentTimeMillis();
    }
    @Override
    public long getRelativeMillis() {
      return timeToWaitForExpiration;
    }
  }

  @Test
  @SuppressWarnings("squid:S2925")
  void set_shouldStoreExpirationInDatabase() throws ServiceException, InterruptedException {
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("testString"), "value1");
    final int timeToWaitForExpiration = 1000;
    input.setExpiration(new MockExpiration(timeToWaitForExpiration));
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    ssdbEphemeralStore.set(input, testLocation);
    Thread.sleep(timeToWaitForExpiration);

    // check key expired
    Assertions.assertEquals(0,  jedisClient.keys("*").size());
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void set_shouldNotStoreExpiration_WhenNotPresent(EphemeralInput input) throws ServiceException {
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    ssdbEphemeralStore.set(input, testLocation);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), testLocation);
    Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());

  }

  @Test
  void shouldStoreKey_WithoutDynamicComponent() throws ServiceException {
    final EphemeralKey ephemeralKey = new EphemeralKey(UUID.randomUUID().toString());
    EphemeralInput input = new EphemeralInput(ephemeralKey, "value");
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    ssdbEphemeralStore.set(input, testLocation);

    final Set<String> keys = jedisClient.keys("*");
    Assertions.assertEquals(1, keys.size());
    final String[] keysArray = keys.toArray(new String[0]);
    final String firstKey = keysArray[0];
    Assertions.assertEquals(
        firstKey, String.join("|", testLocation.getLocation()) + "|" + ephemeralKey.getKey());
  }

  @Test
  void shouldStoreKey_WithDynamicComponent() throws ServiceException {
    final EphemeralKey ephemeralKey = new EphemeralKey(UUID.randomUUID().toString(), "dynamic");
    EphemeralInput input = new EphemeralInput(ephemeralKey, "value");
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    ssdbEphemeralStore.set(input, testLocation);

    final Set<String> keys = jedisClient.keys("*");
    Assertions.assertEquals(1, keys.size());
    final String[] keysArray = keys.toArray(new String[0]);
    final String firstKey = keysArray[0];
    Assertions.assertEquals(
        firstKey,
        String.join("|", testLocation.getLocation()) + "|" + ephemeralKey.getKey() + "|dynamic");
  }

  @Test
  void shouldNotOverrideSameKey_WhenLocationIsDifferent() throws ServiceException {
    final SSDBEphemeralStore store =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final EphemeralKey key = new EphemeralKey(UUID.randomUUID().toString());
    final TestLocation location1 = new TestLocation(new String[] {"1"});
    final TestLocation location2 = new TestLocation(new String[] {"2"});
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
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    final EphemeralInput input =
        new EphemeralInput(new EphemeralKey(UUID.randomUUID().toString()), "aaa");

    Assertions.assertDoesNotThrow(() -> ssdbEphemeralStore.set(input, testLocation));
  }

  @Test
  void has() throws ServiceException {
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("test"), "aaa");

    Assertions.assertDoesNotThrow(() -> ssdbEphemeralStore.set(input, testLocation));
    Assertions.assertTrue(ssdbEphemeralStore.has(input.getEphemeralKey(), testLocation));
  }

  @Test
  void doesNotHave() throws ServiceException {
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    final EphemeralKey key = new EphemeralKey("test");

    Assertions.assertFalse(ssdbEphemeralStore.has(key, testLocation));
  }

  @Test
  void hasReturnsTrue_WhenKeyExists_andValueIsEmptyString() throws ServiceException {
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("test"), "");

    ssdbEphemeralStore.set(input, testLocation);
    Assertions.assertTrue(ssdbEphemeralStore.has(input.getEphemeralKey(), testLocation));
  }

  @Test
  void delete() throws ServiceException {
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    final EphemeralInput input =
        new EphemeralInput(new EphemeralKey(UUID.randomUUID().toString()), "aaa");

    Assertions.assertDoesNotThrow(() -> ssdbEphemeralStore.set(input, testLocation));
    Assertions.assertEquals(
        "aaa", ssdbEphemeralStore.get(input.getEphemeralKey(), testLocation).getValue());
    ssdbEphemeralStore.delete(input.getEphemeralKey(), input.getValue().toString(), testLocation);
    Assertions.assertNull(ssdbEphemeralStore.get(input.getEphemeralKey(), testLocation).getValue());
  }

  @Test
  void update() throws ServiceException {
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    final EphemeralKey ephemeralKey = new EphemeralKey(UUID.randomUUID().toString());
    final EphemeralInput input = new EphemeralInput(ephemeralKey, "aaa");

    Assertions.assertDoesNotThrow(() -> ssdbEphemeralStore.set(input, testLocation));
    Assertions.assertEquals("aaa", ssdbEphemeralStore.get(ephemeralKey, testLocation).getValue());

    final EphemeralInput newInput = new EphemeralInput(ephemeralKey, "bbb");

    Assertions.assertDoesNotThrow(() -> ssdbEphemeralStore.update(newInput, testLocation));
    Assertions.assertEquals("bbb", ssdbEphemeralStore.get(ephemeralKey, testLocation).getValue());

    final Set<String> keys = jedisClient.keys("*");
    Assertions.assertEquals(1, keys.size());
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void update_shouldStoreExpiration_WhenPresent(EphemeralInput input) throws ServiceException {
    input.setExpiration(new AbsoluteExpiration(100L));
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    ssdbEphemeralStore.update(input, testLocation);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), testLocation);
    Assertions.assertEquals(input.getValue() + "|100", ephemeralResult.getValue());
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void update_shouldNotStoreExpiration_WhenNotPresent(EphemeralInput input)
      throws ServiceException {
    final SSDBEphemeralStore ssdbEphemeralStore =
        SSDBEphemeralStore.createWithTestConfig(
            redisContainer.getRedisHost(), redisContainer.getRedisPort());
    final TestLocation testLocation = new TestLocation(new String[] {"ccc"});
    ssdbEphemeralStore.update(input, testLocation);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), testLocation);
    Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());
  }
}

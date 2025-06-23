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
import com.zimbra.cs.ephemeral.EphemeralLocation;
import com.zimbra.cs.ephemeral.EphemeralResult;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
  private TestLocation location;
  private SSDBEphemeralStore ssdbEphemeralStore;

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
    location = new TestLocation(new String[] {UUID.randomUUID().toString()});
    ssdbEphemeralStore = SSDBEphemeralStore.createWithTestConfig(
        redisContainer.getRedisHost(), redisContainer.getRedisPort());
  }
  @AfterEach
  void afterEach() {
    jedisClient.flushAll();
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void get(EphemeralInput input) throws ServiceException {
    ssdbEphemeralStore.set(input, location);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), location);
    Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());
  }

  @Test
  void set_nullValueShouldNotStoreAnything() throws ServiceException {
    String k = null;
    @SuppressWarnings("ConstantValue")
    EphemeralInput input = new EphemeralInput(randomKey(), k);
    ssdbEphemeralStore.set(input, location);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), location);
    Assertions.assertNull(ephemeralResult.getValue());
  }

  private static EphemeralKey randomKey() {
    return new EphemeralKey(UUID.randomUUID().toString());
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void set_shouldStoreExpiration_WhenPresent(EphemeralInput input) throws ServiceException {
    input.setExpiration(new AbsoluteExpiration(100L));
    
    ssdbEphemeralStore.set(input, location);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), location);
    Assertions.assertEquals(input.getValue() + "|100", ephemeralResult.getValue());
  }

  @Test
  void set_shouldNotStoreKeysWithNegativeRelativeExpiration() {
    final MockExpiration expiresNow = new MockExpiration(-1000);
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("testString"), "value1", expiresNow);

    final ServiceException serviceException = Assertions.assertThrows(ServiceException.class,
        () -> ssdbEphemeralStore.set(input, location));

    Assertions.assertEquals("system failure: Cannot store a key with expiration -1 seconds", serviceException.getMessage());
  }

  @Test
  void set_shouldNotStoreKeysWithZeroRelativeExpiration() {
    final MockExpiration expiresNow = new MockExpiration(0);
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("testString"), "value1", expiresNow);

    final ServiceException serviceException = Assertions.assertThrows(ServiceException.class,
        () -> ssdbEphemeralStore.set(input, location));

    Assertions.assertEquals("system failure: Cannot store a key with expiration 0 seconds", serviceException.getMessage());
  }

  @Test
  void shouldOnlyDeleteLocationData() throws ServiceException {
    ssdbEphemeralStore.set(new EphemeralInput(randomKey(), "myValue"), location);
    ssdbEphemeralStore.set(new EphemeralInput(randomKey(), "myValue2"), location);
    final EphemeralLocation otherLocation = new TestLocation(new String[] {UUID.randomUUID().toString()});
    ssdbEphemeralStore.set(new EphemeralInput(randomKey(), "otherLocation"), otherLocation);

    ssdbEphemeralStore.deleteData(location);

    Assertions.assertEquals(1, jedisClient.keys("*").size());
    final String keyinredis = getFirstKeyInRedis();
    final String valueInRedis = jedisClient.get(keyinredis);
    Assertions.assertEquals("otherLocation", valueInRedis);
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
  void set_shouldStoreExpirationInDatabase() throws ServiceException {
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("testString"), "value1");
    final int timeToWaitForExpiration = (int) TimeUnit.HOURS.toMillis(10L);
    input.setExpiration(new MockExpiration(timeToWaitForExpiration));
    
    ssdbEphemeralStore.set(input, location);

    final String key = getFirstKeyInRedis();
    final long ttl = jedisClient.ttl(key);

    Assertions.assertTrue(ttl > 0);
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void set_shouldNotStoreExpiration_WhenNotPresent(EphemeralInput input) throws ServiceException {

    ssdbEphemeralStore.set(input, this.location);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), this.location);
    Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());
    final String key = getFirstKeyInRedis();
    final Long ttl = jedisClient.ttl(key);
    Assertions.assertEquals(-1, ttl);

  }

  @Test
  void shouldStoreKey_WithoutDynamicComponent() throws ServiceException {
    final EphemeralKey ephemeralKey = randomKey();
    EphemeralInput input = new EphemeralInput(ephemeralKey, "value");

    ssdbEphemeralStore.set(input, location);

    final String firstKey = getFirstKeyInRedis();
    Assertions.assertEquals(
        firstKey, String.join("|", location.getLocation()) + "|" + ephemeralKey.getKey());
  }

  private static String getFirstKeyInRedis() {
    final Set<String> keys = jedisClient.keys("*");
    final String[] keysArray = keys.toArray(new String[0]);
    return keysArray[0];
  }

  @Test
  void shouldStoreKey_WithDynamicComponent() throws ServiceException {
    final EphemeralKey ephemeralKey = new EphemeralKey(UUID.randomUUID().toString(), "dynamic");
    EphemeralInput input = new EphemeralInput(ephemeralKey, "value");

    ssdbEphemeralStore.set(input, location);

    final String firstKey = getFirstKeyInRedis();
    Assertions.assertEquals(
        firstKey,
        String.join("|", location.getLocation()) + "|" + ephemeralKey.getKey() + "|dynamic");
  }

  @Test
  void shouldNotOverrideSameKey_WhenLocationIsDifferent() throws ServiceException {
    final EphemeralKey key = randomKey();
    final TestLocation locationA = new TestLocation(new String[] {"A"});
    final TestLocation locationB = new TestLocation(new String[] {"B"});
    final EphemeralInput input1 = new EphemeralInput(key, "value1");
    final EphemeralInput input2 = new EphemeralInput(key, "value2");
    ssdbEphemeralStore.set(input1, locationA);
    ssdbEphemeralStore.set(input2, locationB);

    final EphemeralResult result1 = ssdbEphemeralStore.get(key, locationA);
    final EphemeralResult result2 = ssdbEphemeralStore.get(key, locationB);
    Assertions.assertNotEquals(result1.getValue(), result2.getValue());
  }

  @Test
  void set() {
    final EphemeralInput input =
        new EphemeralInput(randomKey(), "aaa");

    Assertions.assertDoesNotThrow(() -> ssdbEphemeralStore.set(input, location));
  }

  @Test
  void has() throws ServiceException {
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("test"), "aaa");

    Assertions.assertDoesNotThrow(() -> ssdbEphemeralStore.set(input, location));
    Assertions.assertTrue(ssdbEphemeralStore.has(input.getEphemeralKey(), location));
  }

  @Test
  void doesNotHave() throws ServiceException {
    final EphemeralKey key = new EphemeralKey("test");

    Assertions.assertFalse(ssdbEphemeralStore.has(key, location));
  }

  @Test
  void hasReturnsTrue_WhenKeyExists_andValueIsEmptyString() throws ServiceException {
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("test"), "");

    ssdbEphemeralStore.set(input, location);
    Assertions.assertTrue(ssdbEphemeralStore.has(input.getEphemeralKey(), location));
  }

  @Test
  void delete() throws ServiceException {
    final EphemeralInput input =
        new EphemeralInput(randomKey(), "aaa");

    Assertions.assertDoesNotThrow(() -> ssdbEphemeralStore.set(input, location));
    Assertions.assertEquals(
        "aaa", ssdbEphemeralStore.get(input.getEphemeralKey(), location).getValue());
    ssdbEphemeralStore.delete(input.getEphemeralKey(), input.getValue().toString(), location);
    Assertions.assertNull(ssdbEphemeralStore.get(input.getEphemeralKey(), location).getValue());
  }

  @Test
  void update() throws ServiceException {
    final EphemeralKey ephemeralKey = randomKey();
    final EphemeralInput input = new EphemeralInput(ephemeralKey, "aaa");

    Assertions.assertDoesNotThrow(() -> ssdbEphemeralStore.set(input, location));
    Assertions.assertEquals("aaa", ssdbEphemeralStore.get(ephemeralKey, location).getValue());

    final EphemeralInput newInput = new EphemeralInput(ephemeralKey, "bbb");

    Assertions.assertDoesNotThrow(() -> ssdbEphemeralStore.update(newInput, location));
    Assertions.assertEquals("bbb", ssdbEphemeralStore.get(ephemeralKey, location).getValue());

    final Set<String> keys = jedisClient.keys("*");
    Assertions.assertEquals(1, keys.size());
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void update_shouldStoreExpiration_WhenPresent(EphemeralInput input) throws ServiceException {
    input.setExpiration(new AbsoluteExpiration(100L));
    ssdbEphemeralStore.update(input, location);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), location);
    Assertions.assertEquals(input.getValue() + "|100", ephemeralResult.getValue());
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void update_shouldNotStoreExpiration_WhenNotPresent(EphemeralInput input)
      throws ServiceException {
    ssdbEphemeralStore.update(input, location);

    final EphemeralResult ephemeralResult =
        ssdbEphemeralStore.get(input.getEphemeralKey(), location);
    Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());
  }
}

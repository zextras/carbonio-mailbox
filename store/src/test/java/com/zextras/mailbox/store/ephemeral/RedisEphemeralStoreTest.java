/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.zextras.mailbox.testcontainers.RedisExtension;
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
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.RegisterExtension;
import redis.clients.jedis.Jedis;

class RedisEphemeralStoreTest {

  @RegisterExtension
  static final RedisExtension redis = new RedisExtension();

  static Jedis jedisClient;
  private TestLocation location;
  private RedisEphemeralStore redisEphemeralStore;

  @BeforeAll
  static void setUp() {
    jedisClient = new Jedis(redis.getHost(), redis.getPort());
  }

  @AfterAll
  static void tearDown() {
    jedisClient.close();
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
    redisEphemeralStore = RedisEphemeralStore.create(
        redis.getHost(), redis.getPort(), new GenericObjectPoolConfig<>());
  }
  @AfterEach
  void afterEach() {
    jedisClient.flushAll();
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void get(EphemeralInput input) throws ServiceException {
    redisEphemeralStore.set(input, location);

    final EphemeralResult ephemeralResult =
        redisEphemeralStore.get(input.getEphemeralKey(), location);
    Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());
  }

  @Test
  void set_nullValueShouldNotStoreAnything() throws ServiceException {
    String k = null;
    @SuppressWarnings("ConstantValue")
    EphemeralInput input = new EphemeralInput(randomKey(), k);
    redisEphemeralStore.set(input, location);

    final EphemeralResult ephemeralResult =
        redisEphemeralStore.get(input.getEphemeralKey(), location);
    Assertions.assertNull(ephemeralResult.getValue());
  }

  private static EphemeralKey randomKey() {
    return new EphemeralKey(UUID.randomUUID().toString());
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void set_shouldStoreExpiration_AsRedisTtl(EphemeralInput input) throws ServiceException {
    long futureExpiry = System.currentTimeMillis() + 10_000L; // 10 seconds from now
    input.setExpiration(new AbsoluteExpiration(futureExpiry));
    redisEphemeralStore.set(input, location);

    final EphemeralResult ephemeralResult =
        redisEphemeralStore.get(input.getEphemeralKey(), location);
    Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());
  }

  /**
   * CO-3095: a value stored with an expiration must round-trip unchanged. The expiration belongs in
   * the Redis TTL, not appended to the value. Reproduces the production failure where
   * {@code LastLogon} read back "{@code <timestamp>|<epochMillis>}" and failed to parse it.
   */
  @Test
  void get_shouldReturnValueWithoutExpirationSuffix_WhenExpirationPresent()
      throws ServiceException {
    final EphemeralKey key = new EphemeralKey("zimbraLastLogonTimestamp");
    final String lastLogonTimestamp = "20251112020029.000Z";
    final EphemeralInput input = new EphemeralInput(key, lastLogonTimestamp);
    input.setExpiration(new AbsoluteExpiration(System.currentTimeMillis() + 10_000L));

    redisEphemeralStore.set(input, location);

    final EphemeralResult result = redisEphemeralStore.get(key, location);
    Assertions.assertEquals(lastLogonTimestamp, result.getValue());
  }

  /**
   * CO-3095: storing with an expiration must still apply the TTL while keeping the value clean, so
   * the fix for the value format cannot regress key expiration.
   */
  @Test
  void set_shouldNotStoreTtl_IntoValue() throws ServiceException {
    final EphemeralKey key = new EphemeralKey("zimbraLastLogonTimestamp");
    final String value = "20251112020029.000Z";
    final EphemeralInput input = new EphemeralInput(key, value);
    input.setExpiration(new AbsoluteExpiration(System.currentTimeMillis() + 10_000L));

    redisEphemeralStore.set(input, location);

    final String storedKey = getFirstKeyInRedis();
    Assertions.assertEquals(value, jedisClient.get(storedKey));
  }

  @Test
  void set_shouldNotStoreKeysWithNegativeRelativeExpiration() throws ServiceException {
    final MockExpiration expiresNow = new MockExpiration(-1000);
    final EphemeralInput input =
        new EphemeralInput(new EphemeralKey("testString"), "value1", expiresNow);

    redisEphemeralStore.set(input, location);

    Assertions.assertNull(redisEphemeralStore.get(input.getEphemeralKey(), location).getValue(),
        "Expected no value to be stored for negative relative expiration");
  }

  @Test
  void set_shouldNotStoreKeysWithZeroRelativeExpiration() throws ServiceException {
    final MockExpiration expiresNow = new MockExpiration(0);
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("testString"), "value1", expiresNow);

    redisEphemeralStore.set(input, location);

    Assertions.assertNull(redisEphemeralStore.get(input.getEphemeralKey(), location).getValue(),
        "Expected no value to be stored for zero relative expiration");
  }

  @Test
  void deleteData_shouldDeleteLastLogonTimestamp_OnlyForGivenLocation() throws ServiceException {
    final EphemeralKey lastLogon = new EphemeralKey("zimbraLastLogonTimestamp");
    redisEphemeralStore.set(new EphemeralInput(lastLogon, "20251112020029.000Z"), location);
    final EphemeralLocation otherLocation = new TestLocation(new String[] {UUID.randomUUID().toString()});
    redisEphemeralStore.set(new EphemeralInput(lastLogon, "20251112020030.000Z"), otherLocation);

    redisEphemeralStore.deleteData(location);

    Assertions.assertFalse(redisEphemeralStore.has(lastLogon, location));
    Assertions.assertTrue(redisEphemeralStore.has(lastLogon, otherLocation));
  }

  @Test
  void deleteData_shouldLeaveExpiringTokenData_ToBeEvictedByTtl() throws ServiceException {
    final EphemeralKey authToken = new EphemeralKey("zimbraAuthTokens", "1125774878");
    redisEphemeralStore.set(new EphemeralInput(authToken, "carbonio"), location);

    redisEphemeralStore.deleteData(location);

    Assertions.assertTrue(redisEphemeralStore.has(authToken, location));
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
    
    redisEphemeralStore.set(input, location);

    final String key = getFirstKeyInRedis();
    final long ttl = jedisClient.ttl(key);

    Assertions.assertTrue(ttl > 0);
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void set_shouldNotStoreExpiration_WhenNotPresent(EphemeralInput input) throws ServiceException {

    redisEphemeralStore.set(input, this.location);

    final EphemeralResult ephemeralResult =
        redisEphemeralStore.get(input.getEphemeralKey(), this.location);
    Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());
    final String key = getFirstKeyInRedis();
    final Long ttl = jedisClient.ttl(key);
    Assertions.assertEquals(-1, ttl);

  }

  @Test
  void shouldStoreKey_WithoutDynamicComponent() throws ServiceException {
    final EphemeralKey ephemeralKey = randomKey();
    EphemeralInput input = new EphemeralInput(ephemeralKey, "value");

    redisEphemeralStore.set(input, location);

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

    redisEphemeralStore.set(input, location);

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
    redisEphemeralStore.set(input1, locationA);
    redisEphemeralStore.set(input2, locationB);

    final EphemeralResult result1 = redisEphemeralStore.get(key, locationA);
    final EphemeralResult result2 = redisEphemeralStore.get(key, locationB);
    Assertions.assertNotEquals(result1.getValue(), result2.getValue());
  }

  @Test
  void set() {
    final EphemeralInput input =
        new EphemeralInput(randomKey(), "aaa");

    Assertions.assertDoesNotThrow(() -> redisEphemeralStore.set(input, location));
  }

  @Test
  void has() throws ServiceException {
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("test"), "aaa");

    Assertions.assertDoesNotThrow(() -> redisEphemeralStore.set(input, location));
    Assertions.assertTrue(redisEphemeralStore.has(input.getEphemeralKey(), location));
  }

  @Test
  void doesNotHave() throws ServiceException {
    final EphemeralKey key = new EphemeralKey("test");

    Assertions.assertFalse(redisEphemeralStore.has(key, location));
  }

  @Test
  void hasReturnsTrue_WhenKeyExists_andValueIsEmptyString() throws ServiceException {
    final EphemeralInput input = new EphemeralInput(new EphemeralKey("test"), "");

    redisEphemeralStore.set(input, location);
    Assertions.assertTrue(redisEphemeralStore.has(input.getEphemeralKey(), location));
  }

  @Test
  void delete() throws ServiceException {
    final EphemeralInput input =
        new EphemeralInput(randomKey(), "aaa");

    Assertions.assertDoesNotThrow(() -> redisEphemeralStore.set(input, location));
    Assertions.assertEquals(
        "aaa", redisEphemeralStore.get(input.getEphemeralKey(), location).getValue());
    redisEphemeralStore.delete(input.getEphemeralKey(), input.getValue().toString(), location);
    Assertions.assertNull(redisEphemeralStore.get(input.getEphemeralKey(), location).getValue());
  }

  @Test
  void update() throws ServiceException {
    final EphemeralKey ephemeralKey = randomKey();
    final EphemeralInput input = new EphemeralInput(ephemeralKey, "aaa");

    Assertions.assertDoesNotThrow(() -> redisEphemeralStore.set(input, location));
    Assertions.assertEquals("aaa", redisEphemeralStore.get(ephemeralKey, location).getValue());

    final EphemeralInput newInput = new EphemeralInput(ephemeralKey, "bbb");

    Assertions.assertDoesNotThrow(() -> redisEphemeralStore.update(newInput, location));
    Assertions.assertEquals("bbb", redisEphemeralStore.get(ephemeralKey, location).getValue());

    final Set<String> keys = jedisClient.keys("*");
    Assertions.assertEquals(1, keys.size());
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void update_shouldStoreExpiration_AsRedisTtl(EphemeralInput input) throws ServiceException {
    long futureExpiry = System.currentTimeMillis() + 10_000L; // 10 seconds from now
    input.setExpiration(new AbsoluteExpiration(futureExpiry));
    redisEphemeralStore.update(input, location);

    final EphemeralResult ephemeralResult =
        redisEphemeralStore.get(input.getEphemeralKey(), location);
    Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());
  }

  @ParameterizedTest
  @MethodSource("generateInput")
  void update_shouldNotStoreExpiration_WhenNotPresent(EphemeralInput input)
      throws ServiceException {
    redisEphemeralStore.update(input, location);

    final EphemeralResult ephemeralResult =
        redisEphemeralStore.get(input.getEphemeralKey(), location);
    Assertions.assertEquals(input.getValue().toString(), ephemeralResult.getValue());
  }
}

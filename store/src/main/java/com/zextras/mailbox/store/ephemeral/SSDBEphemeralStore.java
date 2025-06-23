/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.ephemeral.EphemeralInput;
import com.zimbra.cs.ephemeral.EphemeralKey;
import com.zimbra.cs.ephemeral.EphemeralLocation;
import com.zimbra.cs.ephemeral.EphemeralResult;
import com.zimbra.cs.ephemeral.EphemeralStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class SSDBEphemeralStore extends EphemeralStore {

  private final JedisPool jedisPool;

  private SSDBEphemeralStore(String host, int port, GenericObjectPoolConfig<Jedis> poolConfig) {
    this.jedisPool = new JedisPool(poolConfig, host, port);
  }

  public static SSDBEphemeralStore createWithTestConfig(String host, int port) {
    GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
    return new SSDBEphemeralStore(host, port, poolConfig);
  }

  public static SSDBEphemeralStore create(
      String host, int port, GenericObjectPoolConfig<Jedis> poolConfig) {
    return new SSDBEphemeralStore(host, port, poolConfig);
  }

  @Override
  public EphemeralResult get(EphemeralKey ephemeralKey, EphemeralLocation location)
      throws ServiceException {
    final String key = getAccessKey(location, ephemeralKey);
    try (Jedis jedis = jedisPool.getResource()) {
      final String gotResult = jedis.get(key);
      return new EphemeralResult(ephemeralKey, gotResult);
    }
  }

  @Override
  public void set(EphemeralInput input, EphemeralLocation location) throws ServiceException {
    final String key = getAccessKey(location, input.getEphemeralKey());
    final Object value = input.getValue();
    if (value != null) {
      final String valueToStore = value.toString();
      final Long expiration = input.getExpiration();
      if (expiration == null) {
        try (Jedis jedis = jedisPool.getResource()) {
          jedis.set(key, valueToStore);
        }
      } else {
        final int expirationSeconds = input.getRelativeExpiration().intValue()/1000;
        if (expirationSeconds <= 0) {
          throw ServiceException.FAILURE("Cannot store a key with expiration " + expirationSeconds + " seconds");
        }
        try (Jedis jedis = jedisPool.getResource()) {
          jedis.setex(key, expirationSeconds, valueToStore + "|" + expiration);
        }
      }

    }
  }

  private String getAccessKey(EphemeralLocation location, EphemeralKey ephemeralKey) {
    String finalKey = getLocationPartKey(location);
    finalKey += "|" + ephemeralKey.getKey();
    if (ephemeralKey.isDynamic()) finalKey += "|" + ephemeralKey.getDynamicComponent();
    return finalKey;
  }

  private String getLocationPartKey(EphemeralLocation location) {
    final String[] path = location.getLocation();
    final ArrayList<String> finalKey = new ArrayList<>(Arrays.stream(path).toList());
    return String.join("|", finalKey);
  }

  @Override
  public void update(EphemeralInput input, EphemeralLocation location) throws ServiceException {
    set(input, location);
  }

  @Override
  public void delete(EphemeralKey key, String value, EphemeralLocation location)
      throws ServiceException {
    final String accessKey = getAccessKey(location, key);
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.del(accessKey);
    }
  }

  @Override
  public boolean has(EphemeralKey key, EphemeralLocation location) throws ServiceException {
    final String accessKey = getAccessKey(location, key);
    try (Jedis jedis = jedisPool.getResource()) {
      final String value = jedis.get(accessKey);
      return value != null;
    }
  }

  @Override
  public void purgeExpired(EphemeralKey key, EphemeralLocation location) throws ServiceException {
    // nothing to do here. SSDB deletes expired keys automagically
  }

  private Set<String> getAllKeys(String pattern, String cursor, Jedis jedisResource) {
    final ScanParams scanParams = new ScanParams().count(100).match(pattern);

    final ScanResult<String> scanResult = jedisResource.scan(cursor, scanParams);
    final HashSet<String> keysSet = new HashSet<>(scanResult.getResult());

    if (!ScanParams.SCAN_POINTER_START.equals(scanResult.getCursor())) {
      keysSet.addAll(getAllKeys(pattern, scanResult.getCursor(), jedisResource));
    }
    return keysSet;
  }

  @Override
  public void deleteData(EphemeralLocation location) {
    try (var jedisClient = jedisPool.getResource()) {
      final String accessKeyPattern = getLocationPartKey(location) + "|*";
      final Set<String> keysToDelete = getAllKeys(accessKeyPattern, ScanParams.SCAN_POINTER_START, jedisClient);
      keysToDelete.forEach(jedisClient::del);
     }
  }

}

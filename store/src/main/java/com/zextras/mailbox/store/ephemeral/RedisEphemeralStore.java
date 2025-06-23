/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ephemeral.EphemeralInput;
import com.zimbra.cs.ephemeral.EphemeralKey;
import com.zimbra.cs.ephemeral.EphemeralLocation;
import com.zimbra.cs.ephemeral.EphemeralResult;
import com.zimbra.cs.ephemeral.EphemeralStore;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class RedisEphemeralStore extends EphemeralStore {

  private final JedisPool jedisPool;

  private RedisEphemeralStore(String host, int port, GenericObjectPoolConfig<Jedis> poolConfig) {
    this.jedisPool = new JedisPool(poolConfig, host, port);
  }

  public static RedisEphemeralStore createWithTestConfig(String host, int port) {
    GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
    return new RedisEphemeralStore(host, port, poolConfig);
  }

  public static RedisEphemeralStore create(
      String host, int port, GenericObjectPoolConfig<Jedis> poolConfig) {
    return new RedisEphemeralStore(host, port, poolConfig);
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
    if (value == null) {
      return;
    }
    final String valueToStore = value.toString();
    final Long expiration = input.getExpiration();
    try (Jedis jedis = jedisPool.getResource()) {
      if (expiration == null) {
        jedis.set(key, valueToStore);
      } else {
        var ttlMillis = input.getRelativeExpiration();
         if (ttlMillis <= 0) {
          ZimbraLog.ephemeral.warn("Cannot store value of key " + key + " with expiration " + ttlMillis + " milliseconds");
          return;
         }
         jedis.psetex(key, ttlMillis, valueToStore + "|" + expiration);
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
    // nothing to do here. Redis deletes expired keys automagically
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

  public static class RedisEphemeralStoreFactory extends Factory {

    private static GenericObjectPoolConfig<Jedis> getPoolConfig() throws ServiceException {
      GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
      Config zimbraConf = Provisioning.getInstance().getConfig();
      int poolSize = zimbraConf.getSSDBResourcePoolSize();
      if (poolSize == 0) {
        poolConfig.setMaxTotal(-1);
      } else {
        poolConfig.setMaxTotal(poolSize);
      }
      long timeout = zimbraConf.getSSDBResourcePoolTimeout();
      if (timeout > 0) {
        poolConfig.setMaxWaitMillis(timeout);
      }
      return poolConfig;
    }

    @Override
    public EphemeralStore getStore() {
      final GenericObjectPoolConfig<Jedis> poolConfig;
      try {
        var parsedURI = new URI(getURL());
        poolConfig = getPoolConfig();
        return RedisEphemeralStore.create(parsedURI.getHost(), parsedURI.getPort(), poolConfig);
      } catch (ServiceException | URISyntaxException e) {
        throw new GenericRedisException("Failed to create EphemeralStore", e);
      }
    }

    @Override
    public void startup() {
      // nothing to do
    }

    @Override
    public void shutdown() {
      // nothing to do
    }

    @Override
    public void test(String url) throws ServiceException {
      // nothing to do
    }
  }

}

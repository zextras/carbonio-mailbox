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
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisEphemeralStore extends EphemeralStore {

  private final JedisPool jedisPool;

  private RedisEphemeralStore(String host, int port, GenericObjectPoolConfig<Jedis> poolConfig) {
    this.jedisPool = new JedisPool(poolConfig, host, port);
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
         jedis.psetex(key, ttlMillis, valueToStore);
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

  @Override
  public void deleteData(EphemeralLocation location) {
    // Only zimbraLastLogonTimestamp is stored without a TTL, so it is the one ephemeral
    // attribute that must be deleted explicitly. Auth/CSRF/JWT tokens all carry an expiration
    // and are evicted by Redis on their own. Deleting this single, deterministically-named key
    // avoids scanning the whole keyspace on every account deletion.
    final EphemeralKey lastLogonKey = new EphemeralKey(Provisioning.A_zimbraLastLogonTimestamp);
    final String accessKey = getAccessKey(location, lastLogonKey);
    try (Jedis jedis = jedisPool.getResource()) {
      jedis.del(accessKey);
    }
  }

  /** Closes the underlying connection pool. Call on shutdown to release resources. */
  public void close() {
    jedisPool.close();
  }

  public static class RedisEphemeralStoreFactory extends Factory {

    private RedisEphemeralStore instance;

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
      synchronized (RedisEphemeralStoreFactory.class) {
        if (instance == null) {
          instance = createStore();
        }
        return instance;
      }
    }
    private RedisEphemeralStore createStore() {
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
    }

    @Override
    public void shutdown() {
      synchronized (RedisEphemeralStoreFactory.class) {
        if (instance != null) {
          instance.close();
          instance = null;
        }
      }
    }

    @Override
    public void test(String url) throws ServiceException {
      // nothing to do
    }
  }

}

/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ephemeral.EphemeralInput;
import com.zimbra.cs.ephemeral.EphemeralKey;
import com.zimbra.cs.ephemeral.EphemeralLocation;
import com.zimbra.cs.ephemeral.EphemeralResult;
import com.zimbra.cs.ephemeral.EphemeralStore;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class SSDBEphemeralStore extends EphemeralStore {

  private final JedisPool jedisPool;

  private SSDBEphemeralStore(String host, int port, GenericObjectPoolConfig poolConfig) {
    this.jedisPool = new JedisPool(poolConfig, host, port);
  }

  public static SSDBEphemeralStore createWithTestConfig(String host, int port) {
    GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
    return new SSDBEphemeralStore(host, port, poolConfig);
  }

  public static SSDBEphemeralStore create(
      String host, int port, GenericObjectPoolConfig poolConfig) {
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
    final String[] path = location.getLocation();
    final ArrayList<String> finalKey = new ArrayList<>(Arrays.stream(path).toList());
    finalKey.add(ephemeralKey.getKey());
    if (ephemeralKey.isDynamic()) finalKey.add(ephemeralKey.getDynamicComponent());
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

  @Override
  public void deleteData(EphemeralLocation location) throws ServiceException {}

  public static class Factory extends EphemeralStore.Factory {

    private static GenericObjectPoolConfig getPoolConfig() throws ServiceException {
      GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
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
      final String prefix = "ssdb:";
      final String customUrl;
      final GenericObjectPoolConfig poolConfig;
      try {
        customUrl = getURL();
        poolConfig = getPoolConfig();
      } catch (ServiceException e) {
        throw new RuntimeException("Failed to get pool config", e);
      }
      final String[] parts = customUrl.substring(prefix.length()).split(":");
      final String host = parts.length > 0 && !parts[0].isEmpty() ? parts[0] : "localhost";
      final int port =
          (parts.length > 1 && !parts[1].isEmpty()) ? Integer.parseInt(parts[1]) : 8888;
      return SSDBEphemeralStore.create(host, port, poolConfig);
    }

    @Override
    public void startup() {}

    @Override
    public void shutdown() {}

    @Override
    public void test(String url) throws ServiceException {}
  }
}

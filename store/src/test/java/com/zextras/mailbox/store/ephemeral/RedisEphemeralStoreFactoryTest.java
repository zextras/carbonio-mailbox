/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.redis.testcontainers.RedisContainer;
import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.store.ephemeral.RedisEphemeralStore.RedisEphemeralStoreFactory;
import com.zextras.mailbox.util.AccountCreator.Factory;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ephemeral.EphemeralKey;
import com.zimbra.cs.ephemeral.EphemeralStore;
import com.zimbra.cs.ephemeral.LdapEntryLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;

class RedisEphemeralStoreFactoryTest extends MailboxTestSuite {

  @Container
  static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

  private static Provisioning provisioning;
  private static Jedis jedisClient;

  @BeforeAll
  static void setup() {
    redisContainer.start();
    jedisClient = new Jedis("redis://localhost:" + redisContainer.getRedisPort());
    provisioning = Provisioning.getInstance();
  }

  @Test
  void getStore_shouldNotThrow_WhenURLValid() throws ServiceException {
    final Account account =
        new Factory(provisioning, mailboxTestExtension.getDefaultDomain()).get().create();
    provisioning
        .getConfig()
        .setEphemeralBackendURL("redis://localhost:" + redisContainer.getRedisPort());
    final RedisEphemeralStoreFactory redisEphemeralStoreFactory = new RedisEphemeralStoreFactory();

    final EphemeralStore store = redisEphemeralStoreFactory.getStore();
    Assertions.assertDoesNotThrow(
        () -> store.get(new EphemeralKey("test"), new LdapEntryLocation(account)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"redis:localhost:", "redis:localhost", "redis:/localhost"})
  void shouldReturnANonWorkingStore_WhenURLInvalid(String prefix) throws ServiceException {
    final Account account =
        new Factory(provisioning, mailboxTestExtension.getDefaultDomain()).get().create();
    provisioning.getConfig().setEphemeralBackendURL(prefix + redisContainer.getRedisPort());
    final RedisEphemeralStoreFactory redisEphemeralStoreFactory = new RedisEphemeralStoreFactory();

    final EphemeralStore store = redisEphemeralStoreFactory.getStore();

    Assertions.assertThrows(
        Exception.class, () -> store.get(new EphemeralKey("test"), new LdapEntryLocation(account)));
  }

  @Test
  void shouldNotCreateAdditionalConnections() throws ServiceException {
    final Account account = new Factory(provisioning, mailboxTestExtension.getDefaultDomain()).get().create();
    provisioning.getConfig().setEphemeralBackendURL("redis://localhost:" + redisContainer.getRedisPort());

    final RedisEphemeralStoreFactory redisEphemeralStoreFactory = new RedisEphemeralStoreFactory();

    final EphemeralStore store = redisEphemeralStoreFactory.getStore();
    store.get(new EphemeralKey("test"), new LdapEntryLocation(account));
    final EphemeralStore store2 = redisEphemeralStoreFactory.getStore();
    store2.get(new EphemeralKey("test"), new LdapEntryLocation(account));
    final EphemeralStore store3 = redisEphemeralStoreFactory.getStore();
    store3.get(new EphemeralKey("test"), new LdapEntryLocation(account));

    // additional connection
    final String[] clientList = jedisClient.clientList().split("\n");

    Assertions.assertEquals(2, clientList.length);
  }
}

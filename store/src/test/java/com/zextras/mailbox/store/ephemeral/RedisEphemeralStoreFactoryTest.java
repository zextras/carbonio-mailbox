/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.testcontainers.RedisExtension;
import com.zextras.mailbox.store.ephemeral.RedisEphemeralStore.RedisEphemeralStoreFactory;
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
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.ResourceLock;
import redis.clients.jedis.Jedis;

@ResourceLock("redis")
class RedisEphemeralStoreFactoryTest extends MailboxTestSuite {

	@RegisterExtension
	static final RedisExtension redis = new RedisExtension();

	private static Provisioning provisioning;
	private static Jedis jedisClient;

	@BeforeAll
	static void setup() {
		jedisClient = new Jedis(redis.getHost(), redis.getPort());
		provisioning = Provisioning.getInstance();
	}

	@Test
	void getStore_shouldNotThrow_WhenURLValid() throws ServiceException {
		final Account account =
				createAccount().create();
		provisioning
				.getConfig()
				.setEphemeralBackendURL("redis://" + redis.getHost() + ":" + redis.getPort());
		final RedisEphemeralStoreFactory redisEphemeralStoreFactory = new RedisEphemeralStoreFactory();

		final EphemeralStore store = redisEphemeralStoreFactory.getStore();
		Assertions.assertDoesNotThrow(
				() -> store.get(new EphemeralKey("test"), new LdapEntryLocation(account)));
	}

	@ParameterizedTest
	@ValueSource(strings = {"redis:localhost:", "redis:localhost", "redis:/localhost"})
	void shouldReturnANonWorkingStore_WhenURLInvalid(String prefix) throws ServiceException {
		final Account account =
				createAccount().create();
		provisioning.getConfig().setEphemeralBackendURL(prefix + redis.getPort());
		final RedisEphemeralStoreFactory redisEphemeralStoreFactory = new RedisEphemeralStoreFactory();

		final EphemeralStore store = redisEphemeralStoreFactory.getStore();

		Assertions.assertThrows(
				Exception.class, () -> store.get(new EphemeralKey("test"), new LdapEntryLocation(account)));
	}

	@Test
	void shouldNotCreateAdditionalConnections() throws ServiceException {
		final Account account = createAccount().create();
		provisioning.getConfig()
				.setEphemeralBackendURL("redis://" + redis.getHost() + ":" + redis.getPort());

		final int clientsBefore = connectedClients();
		final RedisEphemeralStoreFactory redisEphemeralStoreFactory = new RedisEphemeralStoreFactory();

		final EphemeralStore store = redisEphemeralStoreFactory.getStore();
		store.get(new EphemeralKey("test"), new LdapEntryLocation(account));
		final EphemeralStore store2 = redisEphemeralStoreFactory.getStore();
		store2.get(new EphemeralKey("test"), new LdapEntryLocation(account));
		final EphemeralStore store3 = redisEphemeralStoreFactory.getStore();
		store3.get(new EphemeralKey("test"), new LdapEntryLocation(account));

		Assertions.assertEquals(clientsBefore + 1, connectedClients());
	}

	private static int connectedClients() {
		return jedisClient.clientList().split("\n").length;
	}
}

/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.redis.testcontainers.RedisContainer;
import com.zextras.mailbox.MailboxTestSuite;
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

class SSDBEphemeralStoreFactoryTest extends MailboxTestSuite {

	@Container
	static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));
	private static Provisioning provisioning;

	@BeforeAll
	static void setup() {
		redisContainer.start();
		provisioning = Provisioning.getInstance();
	}

	@Test
	void getStore_shouldNotThrow_WhenURLValid() throws ServiceException {
		provisioning.getConfig().setEphemeralBackendURL("ssdb:localhost:" + redisContainer.getRedisPort());
		final SSDBEphemeralStoreFactory ssdbEphemeralStoreFactory = new SSDBEphemeralStoreFactory();
		Assertions.assertDoesNotThrow(ssdbEphemeralStoreFactory::getStore);
	}

	@ParameterizedTest
	@ValueSource(strings = {"ssdb://localhost:", "ssdb:localhost", "ssdb:/localhost"})
	void shouldReturnANonWorkingStore_WhenURLInvalid(String prefix) throws ServiceException {
		final Account account = new Factory(provisioning, mailboxTestExtension.getDefaultDomain()).get()
				.create();
		provisioning.getConfig().setEphemeralBackendURL(prefix + redisContainer.getRedisPort());
		final SSDBEphemeralStoreFactory ssdbEphemeralStoreFactory = new SSDBEphemeralStoreFactory();

		final EphemeralStore store = ssdbEphemeralStoreFactory.getStore();

		Assertions.assertThrows(Exception.class, () -> store.get(new EphemeralKey("test"), new LdapEntryLocation(account)));
	}

}

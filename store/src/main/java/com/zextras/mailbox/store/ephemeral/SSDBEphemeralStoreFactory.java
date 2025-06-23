/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.store.ephemeral;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ephemeral.EphemeralStore;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;

public class SSDBEphemeralStoreFactory extends EphemeralStore.Factory {

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
		final String prefix = "ssdb:";
		final String customUrl;
		final GenericObjectPoolConfig<Jedis> poolConfig;
		try {
			customUrl = getURL();
			poolConfig = getPoolConfig();
		} catch (ServiceException e) {
			throw new SSDBException("Failed to get pool config", e);
		}
		final String[] parts = customUrl.substring(prefix.length()).split(":");
		final String host = parts.length > 0 && !parts[0].isEmpty() ? parts[0] : "localhost";
		final int port =
				(parts.length > 1 && !parts[1].isEmpty()) ? Integer.parseInt(parts[1]) : 8888;
		return SSDBEphemeralStore.create(host, port, poolConfig);
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

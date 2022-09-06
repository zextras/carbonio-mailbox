// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.memcached;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.memcached.ZimbraMemcachedClient;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import net.spy.memcached.DefaultHashAlgorithm;

public class MemcachedConnector {

  private static ZimbraMemcachedClient sTheClient = new ZimbraMemcachedClient();

  /**
   * Returns the one and only memcached client object.
   *
   * @return
   */
  public static ZimbraMemcachedClient getClient() {
    return sTheClient;
  }

  /**
   * Startup the memcached connection. Establish the memcached connection(s) if configured.
   *
   * @throws ServiceException
   */
  public static void startup() throws ServiceException {
    reloadConfig();
  }

  /**
   * Are we currently connected to the memcached servers?
   *
   * @return
   */
  public static boolean isConnected() {
    return sTheClient.isConnected();
  }

  /**
   * Reload the memcached client configuration. Connect to the servers if configured with a
   * non-empty server list. Any old connections are flushed and disconnected.
   *
   * @throws ServiceException
   */
  public static void reloadConfig() throws ServiceException {
    Server server = Provisioning.getInstance().getLocalServer();
    String[] serverList = server.getMultiAttr(Provisioning.A_zimbraMemcachedClientServerList);
    boolean useBinaryProtocol =
        server.getBooleanAttr(Provisioning.A_zimbraMemcachedClientBinaryProtocolEnabled, false);
    String hashAlgorithm =
        server.getAttr(
            Provisioning.A_zimbraMemcachedClientHashAlgorithm,
            DefaultHashAlgorithm.KETAMA_HASH.toString());
    int expirySeconds =
        (int) server.getLongAttr(Provisioning.A_zimbraMemcachedClientExpirySeconds, 86400);
    long timeoutMillis =
        server.getLongAttr(Provisioning.A_zimbraMemcachedClientTimeoutMillis, 10000);
    sTheClient.connect(serverList, useBinaryProtocol, hashAlgorithm, expirySeconds, timeoutMillis);
  }

  /**
   * Shutdown the memcached connection.
   *
   * @throws ServiceException
   */
  public static void shutdown() throws ServiceException {
    sTheClient.disconnect(30000);
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.NetUtil;
import com.zimbra.cs.account.Provisioning;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;

public abstract class ServerConfig {
  private String protocol;
  private boolean ssl;

  private static final int DEFAULT_SHUTDOWN_TIMEOUT = 10;
  private static final int DEFAULT_MAX_THREADS = 1;
  private static final int DEFAULT_MAX_IDLE_TIME = 600;
  private static final int DEFAULT_MAX_CONNECTIONS = 10;
  private static final int DEFAULT_WRITE_CHUNK_SIZE = 8192;
  private static final int DEFAULT_WRITE_TIMEOUT = 10;
  private static final int DEFAULT_THREAD_KEEP_ALIVE_TIME = 60 * 2;
  private static final int DEFAULT_MAX_WRITE_QUEUE_SIZE = LC.nio_max_write_queue_size.intValue();

  public ServerConfig(String protocol, boolean ssl) {
    this.protocol = protocol;
    this.ssl = ssl;
  }

  public String getServerName() {
    return LC.zimbra_server_hostname.value();
  }

  public String getServerVersion() {
    return null;
  }

  public String getBindAddress() {
    return null;
  }

  public abstract int getBindPort();

  public abstract Log getLog();

  /**
   * Returns the max idle time. Connections with no data transmission for longer than this value is
   * subject to terminate.
   *
   * @return max idle time in seconds
   */
  public int getMaxIdleTime() {
    return DEFAULT_MAX_IDLE_TIME;
  }

  public int getMaxThreads() {
    return DEFAULT_MAX_THREADS;
  }

  public String getProtocol() {
    return protocol;
  }

  public boolean isSslEnabled() {
    return ssl;
  }

  public String getGreeting() {
    return getDescription() + " ready";
  }

  public String getGoodbye() {
    return getDescription() + " closing connection";
  }

  /**
   * The message sent back to a client whose new connection is being rejected because the thread
   * pool is currently exhausted.
   */
  public String getConnectionRejected() {
    return null;
  }

  public String getDescription() {
    StringBuilder sb = new StringBuilder();
    String name = getServerName();
    if (name != null && !name.isEmpty()) {
      sb.append(name).append(' ');
    }
    sb.append("Zimbra ");
    String version = getServerVersion();
    if (version != null && !version.isEmpty()) {
      sb.append(version).append(' ');
    }
    return sb.append(getProtocol()).append(" server").toString();
  }

  public String[] getMailboxdSslProtocols() {
    return getConfigAttr(Provisioning.A_zimbraMailboxdSSLProtocols);
  }

  public String[] getSslExcludedCiphers() {
    return getConfigAttr(Provisioning.A_zimbraSSLExcludeCipherSuites);
  }

  public String[] getSslIncludedCiphers() {
    return getConfigAttr(Provisioning.A_zimbraSSLIncludeCipherSuites);
  }

  private String[] getConfigAttr(String key) {
    try {
      return Provisioning.getInstance().getLocalServer().getMultiAttr(key);
    } catch (ServiceException e) {
      getLog().warn("Unable to get attribute: " + key, e);
      return null;
    }
  }

  /**
   * Returns the server shutdown timeout. Upon a shutdown request, the server waits for handlers to
   * cleanly terminate as long as this timeout, then halts remaining handlers.
   *
   * @return shutdown timeout in seconds
   */
  public int getShutdownTimeout() {
    return DEFAULT_SHUTDOWN_TIMEOUT;
  }

  /**
   * Returns the max number of concurrent connections allowed. New connections exceeding this limit
   * are rejected.
   *
   * @return max number of connections
   */
  public int getMaxConnections() {
    return DEFAULT_MAX_CONNECTIONS;
  }

  public int getWriteChunkSize() {
    return DEFAULT_WRITE_CHUNK_SIZE;
  }

  public int getWriteTimeout() {
    return DEFAULT_WRITE_TIMEOUT;
  }

  public int getThreadKeepAliveTime() {
    return DEFAULT_THREAD_KEEP_ALIVE_TIME;
  }

  public int getNioMaxWriteQueueDelay() {
    return getWriteTimeout() * (int) Constants.MILLIS_PER_SECOND;
  }

  public int getNioMaxWriteQueueSize() {
    return DEFAULT_MAX_WRITE_QUEUE_SIZE;
  }

  public ServerSocket getServerSocket() throws ServiceException {
    return isSslEnabled()
        ? NetUtil.getSslTcpServerSocket(
            getBindAddress(),
            getBindPort(),
            getSslExcludedCiphers(),
            getSslIncludedCiphers(),
            getMailboxdSslProtocols())
        : NetUtil.getTcpServerSocket(getBindAddress(), getBindPort());
  }

  public ServerSocketChannel getServerSocketChannel() throws ServiceException {
    return NetUtil.getNioServerSocket(getBindAddress(), getBindPort()).getChannel();
  }

  protected String getAttr(String key, String defaultValue) {
    try {
      return getLocalServer().getAttr(key, defaultValue);
    } catch (ServiceException e) {
      getLog().warn("Unable to get server attribute: " + key, e);
      return defaultValue;
    }
  }

  protected int getIntAttr(String key, int defaultValue) {
    try {
      return getLocalServer().getIntAttr(key, defaultValue);
    } catch (ServiceException e) {
      getLog().warn("Unable to get server attribute: " + key, e);
      return defaultValue;
    }
  }

  protected boolean getBooleanAttr(String key, boolean defaultValue) {
    try {
      return getLocalServer().getBooleanAttr(key, defaultValue);
    } catch (ServiceException e) {
      getLog().warn("Unable to get server attribute: " + key, e);
      return defaultValue;
    }
  }

  protected com.zimbra.cs.account.Server getLocalServer() throws ServiceException {
    return Provisioning.getInstance().getLocalServer();
  }

  protected com.zimbra.cs.account.Config getGlobalConfig() throws ServiceException {
    return Provisioning.getInstance().getConfig();
  }

  public boolean isServiceEnabled() {
    try {
      return Provisioning.getInstance()
          .getLocalServer()
          .getBooleanAttr(Provisioning.A_zimbraUserServicesEnabled, true);
    } catch (ServiceException e) {
      getLog().error("Unabled to determine the service availability", e);
      return false;
    }
  }

  public String[] getThottleIgnoredHosts() throws ServiceException {
    return getAddrListCsv(getLocalServer().getMultiAttr(Provisioning.A_zimbraThrottleSafeHosts));
  }

  public String[] getThrottleWhitelist() throws ServiceException {
    return getAddrListCsv(getLocalServer().getMultiAttr(Provisioning.A_zimbraThrottleWhitelist));
  }

  public String getKeystorePath() {
    return LC.mailboxd_keystore.value();
  }

  public String getKeystorePassword() {
    return LC.mailboxd_keystore_password.value();
  }

  public static String[] getAddrListCsv(String[] addrCsvs) {
    ArrayList<String> addrList = new ArrayList<String>(addrCsvs.length);
    for (String addrCsv : addrCsvs) {
      if (addrCsv != null && addrCsv.length() > 0) {
        String[] addrs = addrCsv.split(",");
        for (String addr : addrs) {
          addrList.add(addr);
        }
      }
    }
    return addrList.toArray(new String[addrList.size()]);
  }
}

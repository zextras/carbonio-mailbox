// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import static com.zimbra.cs.account.Provisioning.*;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.server.ServerConfig;
import com.zimbra.cs.util.BuildInfo;
import com.zimbra.cs.util.Config;
import java.util.Arrays;

public class ImapConfig extends ServerConfig {
  private static final String PROTOCOL = "IMAP4rev1";
  private static final int DEFAULT_MAX_MESSAGE_SIZE = 100 * 1024 * 1024;

  public ImapConfig(boolean ssl) {
    super(PROTOCOL, ssl);
  }

  @Override
  public String getServerName() {
    return getAttr(A_zimbraImapAdvertisedName, LC.zimbra_server_hostname.value());
  }

  @Override
  public String getServerVersion() {
    return getBooleanAttr(A_zimbraImapExposeVersionOnBanner, false) ? BuildInfo.VERSION : null;
  }

  @Override
  public String getBindAddress() {
    return getAttr(isSslEnabled() ? A_zimbraImapSSLBindAddress : A_zimbraImapBindAddress, null);
  }

  @Override
  public int getBindPort() {
    return isSslEnabled()
        ? getIntAttr(A_zimbraImapSSLBindPort, Config.D_IMAP_SSL_BIND_PORT)
        : getIntAttr(A_zimbraImapBindPort, Config.D_IMAP_BIND_PORT);
  }

  @Override
  public int getWriteTimeout() {
    return LC.imap_write_timeout.intValue();
  }

  @Override
  public int getWriteChunkSize() {
    return LC.imap_write_chunk_size.intValue();
  }

  /**
   * Returns the max idle timeout for unauthenticated connections.
   *
   * @return max idle timeout in seconds
   */
  @Override
  public int getMaxIdleTime() {
    return LC.imap_max_idle_time.intValue();
  }

  /**
   * Returns the max idle timeout for authenticated connections.
   *
   * @return max idle timeout in seconds
   */
  public int getAuthenticatedMaxIdleTime() {
    return LC.imap_authenticated_max_idle_time.intValue();
  }

  @Override
  public int getMaxThreads() {
    return getIntAttr(A_zimbraImapNumThreads, super.getMaxThreads());
  }

  @Override
  public int getMaxConnections() {
    return getIntAttr(A_zimbraImapMaxConnections, super.getMaxConnections());
  }

  @Override
  public Log getLog() {
    return ZimbraLog.imap;
  }

  @Override
  public String getConnectionRejected() {
    return "* BYE " + getDescription() + " closing connection; service busy";
  }

  @Override
  public int getShutdownTimeout() {
    return getIntAttr(A_zimbraImapShutdownGraceSeconds, super.getShutdownTimeout());
  }

  @Override
  public int getThreadKeepAliveTime() {
    return LC.imap_thread_keep_alive_time.intValue();
  }

  public boolean isCleartextLoginEnabled() {
    return getBooleanAttr(A_zimbraImapCleartextLoginEnabled, false);
  }

  public boolean isSaslGssapiEnabled() {
    return getBooleanAttr(A_zimbraImapSaslGssapiEnabled, false);
  }

  public boolean isCapabilityDisabled(String name) {
    String key =
        isSslEnabled() ? A_zimbraImapSSLDisabledCapability : A_zimbraImapDisabledCapability;
    try {
      return Arrays.asList(getLocalServer().getMultiAttr(key)).contains(name);
    } catch (ServiceException e) {
      getLog().warn("Unable to get server attribute: " + key, e);
      return false;
    }
  }

  public int getMaxRequestSize() {
    return getIntAttr(A_zimbraImapMaxRequestSize, LC.imap_max_request_size.intValue());
  }

  /**
   * @return maximum message size where 0 means "no limit"
   */
  public long getMaxMessageSize() throws ServiceException {
    if (LC.imap_max_message_size.value() == null || LC.imap_max_message_size.value().isEmpty()) {
      return Provisioning.getInstance()
          .getConfig()
          .getLongAttr(A_zimbraMtaMaxMessageSize, DEFAULT_MAX_MESSAGE_SIZE);
    } else {
      return LC.imap_max_message_size.longValue();
    }
  }
}

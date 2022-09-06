// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.pop3;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailclient.MailConfig;
import com.zimbra.cs.mailclient.util.Config;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/** Represents POP3 mail client configuration. */
public class Pop3Config extends MailConfig {
  /** POP3 configuration protocol name */
  public static final String PROTOCOL = "pop3";

  /** Default port for POP3 plain text connection */
  public static final int DEFAULT_PORT = 110;

  /** Default port for POP3 SSL connection */
  public static final int DEFAULT_SSL_PORT = 995;

  /**
   * Loads POP3 configuration properties from the specified file.
   *
   * @param file the configuration properties file
   * @return the <tt>Pop3Config</tt> for the properties
   * @throws IOException if an I/O error occurred
   */
  public static Pop3Config load(File file) throws IOException {
    Properties props = Config.loadProperties(file);
    Pop3Config config = new Pop3Config();
    config.applyProperties(props);
    return config;
  }

  /** Creates a new {@link Pop3Config}. */
  public Pop3Config() {
    super(ZimbraLog.pop_client);
  }

  /**
   * Creates a new {@link Pop3Config} for the specified server host.
   *
   * @param host the server host name
   */
  public Pop3Config(String host) {
    super(ZimbraLog.pop_client, host);
    setLogger(ZimbraLog.pop_client);
  }

  /**
   * Returns the POP3 protocol name (value of {@link #PROTOCOL}).
   *
   * @return the POP3 protocol name
   */
  @Override
  public String getProtocol() {
    return PROTOCOL;
  }

  /**
   * Returns the POP3 server port number. If not set, the default is {@link #DEFAULT_PORT} for a
   * plain text connection and {@link #DEFAULT_SSL_PORT} for an SSL connection.
   *
   * @return the POP3 server port number
   */
  @Override
  public int getPort() {
    int port = super.getPort();
    if (port != -1) return port;
    return getSecurity() == Security.SSL ? DEFAULT_SSL_PORT : DEFAULT_PORT;
  }
}

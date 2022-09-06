// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import com.zimbra.common.calendar.WellKnownTimeZones;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.extension.ExtensionUtil;
import com.zimbra.cs.stats.ZimbraPerf;
import com.zimbra.cs.store.StoreManager;
import com.zimbra.cs.util.MemoryStats;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

public class ImapDaemon {

  public static final String IMAPD_LOG4J_CONFIG = "/opt/zextras/conf/imapd.log4j.properties";
  /**
   * When starting IMAP(S) from ImapDaemon, a System property with the following key will be set
   * with a value of "false". Code that needs to know if IMAP(S) servers are being controlled via
   * mailboxd should use the static isRunningImapInsideMailboxd() function, which makes use of this
   * property, to make this determination.
   */
  protected static final String IMAP_SERVER_EMBEDDED = "imap.server.embedded";

  private ImapServer imapServer, imapSSLServer;

  private ImapServer startImapServer(boolean ssl) throws ServiceException {
    RemoteImapConfig config = new RemoteImapConfig(ssl);
    ZimbraLog.imap.info("Starting IMAP server, port=%d", config.getBindPort());
    ImapServer server =
        LC.nio_imap_enabled.booleanValue() ? new NioImapServer(config) : new TcpImapServer(config);
    server.start();
    return server;
  }

  private int startServers() throws ServiceException {
    System.setProperty(IMAP_SERVER_EMBEDDED, "false");
    int cnt = 0;
    if (isEnabled(Provisioning.A_zimbraRemoteImapServerEnabled)) {
      imapServer = startImapServer(false);
      cnt += 1;
    } else {
      ZimbraLog.imap.info("%s is FALSE", Provisioning.A_zimbraRemoteImapServerEnabled);
    }
    if (isEnabled(Provisioning.A_zimbraRemoteImapSSLServerEnabled)) {
      imapSSLServer = startImapServer(true);
      cnt += 1;
    } else {
      ZimbraLog.imap.info("%s is FALSE", Provisioning.A_zimbraRemoteImapSSLServerEnabled);
    }

    return cnt;
  }

  private void stopServer(ImapServer server) {
    try {
      if (server != null) {
        ZimbraLog.imap.info("Stopping IMAP server, port=%d", server.getConfig().getBindPort());
        server.stop(10); // TODO configure wait time
      }
    } catch (ServiceException e) {
      ZimbraLog.imap.error("stopServer", e);
    }
  }

  private void stopServers() {
    stopServer(imapServer);
    stopServer(imapSSLServer);
  }

  public static void main(String[] args) {
    try {
      Properties props = new Properties();
      try (FileInputStream fisLog4j = new FileInputStream(IMAPD_LOG4J_CONFIG)) {
        props.load(fisLog4j);
      }
      PropertyConfigurator.configure(props);
      String imapdClassStore = LC.imapd_class_store.value();
      try {
        StoreManager.getInstance(imapdClassStore).startup();
      } catch (IOException e) {
        throw ServiceException.FAILURE(
            String.format("Unable to initialize StoreManager: %s", imapdClassStore), e);
      }

      if (isZimbraImapEnabled()) {
        ZimbraPerf.prepare(ZimbraPerf.ServerID.IMAP_DAEMON);
        MemoryStats.startup();
        ZimbraPerf.initialize(ZimbraPerf.ServerID.IMAP_DAEMON);

        String tzFilePath = LC.timezone_file.value();
        try {
          File tzFile = new File(tzFilePath);
          WellKnownTimeZones.loadFromFile(tzFile);
        } catch (Throwable t) {
          ZimbraLog.imap.error("Unable to load timezones from %s.", tzFilePath, t);
          errorExit("ImapDaemon: imapd service was unable to intialize timezones EXITING.");
        }

        // Note: This loads the LicenseExtension if present on the system in the case of NETWORK
        // edition
        //       and for the FOSS edition simply prints a WARN level log statement as follows:
        //       2018-01-10 11:49:56,792 WARN  [main] [] extensions - unable to locate extension
        // class com.zimbra.cs.network.license.LicenseExtension, not found
        ExtensionUtil.init("com.zimbra.cs.network.license.LicenseExtension");

        ImapDaemon daemon = new ImapDaemon();
        int numStarted = daemon.startServers();

        if (numStarted > 0) {
          Runtime.getRuntime()
              .addShutdownHook(
                  new Thread() {
                    @Override
                    public void run() {
                      ZimbraLog.imap.info("Shutting down servers");
                      daemon.stopServers();
                    }
                  });
        } else {
          errorExit(
              "ImapDaemon: No servers started. Check zimbraRemoteImapServerEnabled and"
                  + " zimbraRemoteImapSSLServerEnabled");
        }
        if (!isMemberOfPool()) {
          ZimbraLog.imap.warn(
              "ImapDaemon: This server not member of pool. Check"
                  + " zimbraReverseProxyUpstreamImapServers.");
        }
      } else {
        errorExit(
            "ImapDaemon: imapd service is not enabled on this server. Check zimbraServiceEnabled.");
      }
    } catch (Exception e) {
      System.err.println("ImapDaemon: " + e);
      e.printStackTrace(System.err);
    }
  }

  @SuppressWarnings("PMD.DoNotCallSystemExit")
  private static void errorExit(String msg) {
    ZimbraLog.imap.warn(msg);
    System.err.println(msg);
    System.exit(1);
  }

  private static boolean isEnabled(String key) throws ServiceException {
    return Provisioning.getInstance().getLocalServer().getBooleanAttr(key, false);
  }

  private static boolean isMemberOfPool() throws ServiceException {
    String localServer = Provisioning.getInstance().getLocalServer().getName();
    String[] imapServers =
        Provisioning.getInstance().getLocalServer().getReverseProxyUpstreamImapServers();
    for (String server : imapServers) {
      if (localServer.equals(server)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isZimbraImapEnabled() throws ServiceException {
    String[] enabledServices =
        Provisioning.getInstance()
            .getLocalServer()
            .getMultiAttr(Provisioning.A_zimbraServiceEnabled);
    for (String service : enabledServices) {
      if ("imapd".equals(service)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return true if IMAP(S) servers are being controlled by mailboxd and false if they are being
   *     controlled by ImapDaemon. This uses s System property which is not set when mailboxd is
   *     started and which is set to an explicit value of "false" by the ImapDaemon startup code.
   */
  public static boolean isRunningImapInsideMailboxd() {
    return System.getProperty(ImapDaemon.IMAP_SERVER_EMBEDDED, "true").equals("true");
  }
}

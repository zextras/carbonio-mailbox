// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.server;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.net.SocketFactories;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.util.Zimbra;
import com.zimbra.znative.IO;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.jetty.ee8.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

public class MailboxServer {

  private static Server singleton;
  private final Server server;

	MailboxServer(Server server) {
		this.server = server;
    singleton = server;
	}

	public static class InstantiationException extends Exception {

		@Serial
		private static final long serialVersionUID = 3614086690444919273L;

		public InstantiationException(Throwable cause) {
			super("Failed to create Jetty server", cause);
		}
	}

  /**
   * Starts server and joins on same thread
   * @throws Exception
   */
  public void startJoin() throws Exception {
    start();
    server.join();
  }

  /**
   * Starts server in another thread
   * @throws Exception
   */
  public void start() throws Exception {
    ZimbraLog.misc.info("Starting mailbox server");
    initDependencies(); // old FirstServlet#init
    Zimbra.startup();
    server.start();
    com.zextras.carbonio.systemd.SystemdNotify.ready("mailbox ready");
    ZimbraLog.misc.info("Mailbox server started");
  }

  public void stop() {
    try {
      if (server != null) server.stop();
      ZimbraLog.misc.info("Shutting down mailbox server");
      Zimbra.shutdown();
      ZimbraLog.misc.info("Mailbox server shut down");
    } catch (Exception e) {
      ZimbraLog.misc.error("Failed to stop mailbox server", e);
    }
  }


  // From here on: Code moved from FirstServlet
  private static void initDependencies() {
    try {
      System.setProperty("javax.net.ssl.keyStore", LC.mailboxd_keystore.value());
      System.setProperty("javax.net.ssl.keyStorePassword", LC.mailboxd_keystore_password.value());
      System.setProperty("javax.net.ssl.trustStorePassword",
          LC.mailboxd_truststore_password.value());

      SocketFactories.registerProtocolsServer();
      setupOutputRotation();
    } catch (Throwable t) {
      System.err.println("PrivilegedServlet init failed");
      t.printStackTrace(System.err);
      Runtime.getRuntime().halt(1);
    }
  }

  private static Timer sOutputRotationTimer;

  private static void doOutputRotation() {
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    String suffix = sdf.format(now);
    String current = LC.mailboxd_output_file.value();
    String rotateTo = current + "." + suffix;
    try {
      new File(current).renameTo(new File(rotateTo));
      IO.setStdoutStderrTo(current);
    } catch (IOException ioe) {
      System.err.println("WARN: rotate stdout stderr failed: " + ioe);
      ioe.printStackTrace();
    }
  }

  private static void setupOutputRotation()
      throws SecurityException {
    long configMillis = LC.mailboxd_output_rotate_interval.intValue() * 1000;

    if (configMillis <= 0) {
      return;
    }
    sOutputRotationTimer = new Timer("Timer-OutputRotation");
    GregorianCalendar now = new GregorianCalendar();
    long millisSinceEpoch = now.getTimeInMillis();
    long dstOffset = now.get(Calendar.DST_OFFSET);
    long zoneOffset = now.get(Calendar.ZONE_OFFSET);
    long millisSinceEpochLocal = millisSinceEpoch + dstOffset + zoneOffset;
    long firstRotateInMillis = configMillis - (millisSinceEpochLocal % configMillis);
    TimerTask tt = new TimerTask() {
      public void run() {
        try {
          doOutputRotation();
        } catch (Throwable e) {
          if (e instanceof OutOfMemoryError) {
            Zimbra.halt("Caught out of memory error", e);
          }
          System.err.println("WARN: Caught exception in FirstServlet timer " + e);
          e.printStackTrace();
        }
      }
    };
    sOutputRotationTimer.scheduleAtFixedRate(tt, firstRotateInMillis, configMillis);
  }

  /**
   * Registers an additional {@link ServletContextHandler} on the running server by adding its core
   * context handler to the top-level handler collection (the {@code Handler.Sequence} assembled in
   * {@link MailboxServerBuilder}).
   *
   * <p>Intended to be called during startup — e.g. from an extension's init, which runs inside
   * {@code Zimbra.startup()} before {@link #start()} calls {@code server.start()} — so the new
   * context is started as part of the normal server lifecycle. If the server is already started,
   * the context is started explicitly.
   *
   * <p>The registered handler only serves requests matching its context path (and virtual hosts).
   * Set {@code contextHandler.setVirtualHosts(...)} to pin it to a specific connector.
   */
  public static void register(ServletContextHandler contextHandler) throws Exception {
    Handler current = singleton.getHandler();
    while (current instanceof Handler.Wrapper wrapper) {
      current = wrapper.getHandler();
    }
    if (!(current instanceof Handler.Collection collection)) {
      throw new IllegalStateException(
          "Cannot register context handler: expected a Handler.Collection at the root of the handler"
              + " tree but found " + (current == null ? "null" : current.getClass().getName()));
    }
    final var coreContextHandler = contextHandler.getCoreContextHandler();
    collection.addHandler(coreContextHandler);
    if (singleton.isStarted() && !coreContextHandler.isStarted()) {
      coreContextHandler.start();
    }
  }
}

// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import com.zextras.mailbox.MailboxServer.Builder;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.net.SocketFactories;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.util.Zimbra;
import com.zimbra.znative.IO;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;

public class Mailbox {

	private static final String LOCALCONFIG = "localconfig";
	private static final String DRYRUN = "dryRun";
	private final Provisioning provisioning;
	private Server server;

	public Mailbox(Provisioning provisioning) {
		this.provisioning = provisioning;
	}

	public void start() throws Exception {
		Builder mailboxServerBuilder = new Builder(provisioning.getConfig(),
				provisioning.getLocalServer());

		final Server server = mailboxServerBuilder.build();

		initDependencies(); // old FirstServlet#init
		Zimbra.startup();
		server.start();
		server.join();
	}

	public void stop() {
		try {
			if (server != null) server.stop();
			ZimbraLog.misc.info("Shutting down mailbox server");
			Zimbra.shutdown();
		} catch (Exception e) {
			ZimbraLog.misc.error("Failed to stop mailbox server", e);
		}
	}

	public static void main(String[] args) throws Exception {
		Options options = getOptions();
		CommandLineParser parser = new GnuParser();
		CommandLine commandLine = parser.parse(options, args);

		if (commandLine.hasOption(LOCALCONFIG)) {
			System.setProperty("zimbra.config", commandLine.getOptionValue(LOCALCONFIG));
		}
		new Mailbox(Provisioning.getInstance()).start();

	}

	private static Options getOptions() {
		Options options = new Options();

		Option dryRun = new Option(DRYRUN, true, "If set does not start the server");
		dryRun.setRequired(false);
		options.addOption(dryRun);

		return options;
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


}

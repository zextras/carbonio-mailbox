// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import com.zextras.mailbox.MailboxServer.Builder;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.util.Zimbra;
import javax.servlet.UnavailableException;
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

  public static void main(String[] args) throws Exception {
    Options options = getOptions();
    CommandLineParser parser = new GnuParser();
    CommandLine commandLine = parser.parse(options, args);

    if (commandLine.hasOption(LOCALCONFIG)) {
      System.setProperty("zimbra.config", commandLine.getOptionValue(LOCALCONFIG));
    }

    Builder mailboxServerBuilder = new Builder(Provisioning.getInstance().getConfig(), Provisioning.getInstance().getLocalServer());

    final Server server = mailboxServerBuilder.build();

    if (commandLine.hasOption(DRYRUN)) {
      return;
    }

    server.start();
    server.addLifeCycleListener(new LifeCycle.Listener() {
      @Override
      public void lifeCycleStopping(LifeCycle event) {
				try {
					Zimbra.shutdown(); // clean shutdown of pools, timers, etc.
				} catch (ServiceException e) {
          ZimbraLog.misc.error("Failed to shutdown connections", e);
				}
			}
      @Override public void lifeCycleStarted(LifeCycle event) {
        try {
          Zimbra.startup();
          ZimbraLog.misc.info("Mailbox dependencies started.");
        } catch (OutOfMemoryError e) {
          Zimbra.halt("out of memory", e);
        }
      }
    });
    server.start();
    server.join();
  }

  private static Options getOptions() {
    Options options = new Options();

    Option dryRun =  new Option(DRYRUN,true, "If set does not start the server");
    dryRun.setRequired(false);
    options.addOption(dryRun);

    return options;
  }




}

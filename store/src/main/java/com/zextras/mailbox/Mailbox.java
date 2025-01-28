// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import com.zextras.mailbox.MailboxServer.Builder;
import com.zimbra.cs.account.Provisioning;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Server;

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

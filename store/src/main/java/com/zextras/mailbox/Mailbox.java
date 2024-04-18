// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import com.zextras.mailbox.LikeXmlJettyServer.Builder;
import com.zimbra.cs.account.Provisioning;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Server;

public class Mailbox {


  private static final String WEB_DESCRIPTOR = "webDescriptor";
  private static final String LOCALCONFIG = "localconfig";
  private static final String WEBAPP = "webApp";
  private static final String DRYRUN = "dryRun";

  public static void main(String[] args) throws Exception {
    Options options = getOptions();
    CommandLineParser parser = new GnuParser();
    CommandLine commandLine = parser.parse(options, args);

    if (commandLine.hasOption(LOCALCONFIG)) {
      System.setProperty("zimbra.config", commandLine.getOptionValue(LOCALCONFIG));
    }

    Builder builder = new Builder(Provisioning.getInstance().getConfig());
    if (commandLine.hasOption(WEB_DESCRIPTOR)) {
      builder = builder.withWebDescriptor(commandLine.getOptionValue(WEB_DESCRIPTOR));
    }

    if (commandLine.hasOption(WEBAPP)) {
      builder = builder.withWebApp(commandLine.getOptionValue(WEBAPP));
    }

    final Server server = builder.build();

    if (commandLine.hasOption(DRYRUN)) {
      return;
    }

    server.start();
    server.join();
  }

  private static Options getOptions() {
    Option webDescriptor =  new Option(Mailbox.WEB_DESCRIPTOR,true, "Location to web descriptor");
    webDescriptor.setRequired(false);
    Options options = new Options();
    options.addOption(webDescriptor);

    Option localconfig =  new Option(LOCALCONFIG,true, "Location to localconfig");
    localconfig.setRequired(false);
    options.addOption(localconfig);

    Option webAppDirectory =  new Option(WEBAPP,true, "Location web app directory");
    webAppDirectory.setRequired(false);
    options.addOption(webAppDirectory);

    Option dryRun =  new Option(DRYRUN,true, "If set does not start the server");
    dryRun.setRequired(false);
    options.addOption(dryRun);

    return options;
  }




}

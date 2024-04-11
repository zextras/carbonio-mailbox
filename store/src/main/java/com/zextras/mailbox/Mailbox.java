// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class Mailbox {

  private static final int APP_USER_SERVER_PORT = 7070;
  private static final int APP_ADMIN_SERVER_PORT = 7071;
  private static final String WEB_DESCRIPTOR = "webDescriptor";
  private static final String LOCALCONFIG = "localconfig";

  public static void main(String[] args) throws Exception {
    System.setProperty("zimbra.native.required", "false");
    Options options = getOptions();
    CommandLineParser parser = new GnuParser();
    CommandLine commandLine = parser.parse(options, args);

    if (commandLine.hasOption(LOCALCONFIG)) {
      System.setProperty("zimbra.config", commandLine.getOptionValue(LOCALCONFIG));
    }

    String webDescriptor = "store/conf/web-dev.xml";
    if (commandLine.hasOption(WEB_DESCRIPTOR)) {
      webDescriptor = commandLine.getOptionValue(WEB_DESCRIPTOR);
    }

    WebAppContext webAppContext = new WebAppContext();
    webAppContext.setDescriptor(webDescriptor);
    webAppContext.setResourceBase("/");
    webAppContext.setContextPath("/service");

    Server server = new Server(APP_USER_SERVER_PORT);
    server.setHandler(webAppContext);
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

    return options;
  }

}

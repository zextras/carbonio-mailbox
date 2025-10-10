// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import com.zextras.mailbox.server.MailboxServer;
import com.zextras.mailbox.server.MailboxServerBuilder;
import com.zimbra.cs.account.Provisioning;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

public class Mailbox {

	private static final String LOCALCONFIG = "localconfig";


	public static void main(String[] args) throws Exception {
		Options options = new Options();
		CommandLineParser parser = new GnuParser();
		CommandLine commandLine = parser.parse(options, args);

		if (commandLine.hasOption(LOCALCONFIG)) {
			System.setProperty("zimbra.config", commandLine.getOptionValue(LOCALCONFIG));
		}
		final Provisioning provisioning = Provisioning.getInstance();
		final MailboxServer mailboxServer = new MailboxServerBuilder(provisioning.getConfig(),
				provisioning.getLocalServer()).create();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				mailboxServer.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
		mailboxServer.startJoin();
	}


}

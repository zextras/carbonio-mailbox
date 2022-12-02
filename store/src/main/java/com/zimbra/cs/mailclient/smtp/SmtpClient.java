// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.smtp;

import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.zimbra.common.util.Log;
import com.zimbra.cs.mailclient.MailClient;
import com.zimbra.cs.mailclient.MailConfig;

public final class SmtpClient extends MailClient {

    private final Options options = new Options();

    protected SmtpClient() {
        super(new SmtpConfig());

        options.addOption("p", "port", true, "SMTP server port (default is " + SmtpConfig.DEFAULT_PORT +
                " or " + SmtpConfig.DEFAULT_SSL_PORT + " for SSL)");
        options.addOption("u", "user", true, "SMTP-AUTH: username");
        options.addOption("w", "password", true, "SMTP-AUTH: password");
        options.addOption("r", "realm", true, "SMTP-AUTH: realm");
        options.addOption("m", "mechanism", true, "SMTP-AUTH: mechanism (LOGIN|PLAIN|CRAM-MD5|DIGEST-MD5)");
        options.addOption("s", "ssl", false, "enable SSL (SMTPS)");
        options.addOption("d", "debug", false, "enable debug output");
        options.addOption("h", "help", false, "print this help message");
    }

    @Override
    protected void printUsage(PrintStream ps) {
        usage();
    }

    private void usage() {
        HelpFormatter help = new HelpFormatter();
        help.setWidth(100);
        help.printHelp("zmjava " + getClass().getName() + " [OPTIONS] [HOSTNAME]", options);
    }

    @Override
    protected void parseArguments(String[] args, MailConfig config) {
        CommandLineParser parser = new GnuParser();
        CommandLine cl = null;
        try {
            cl = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (cl.hasOption('p')) {
            config.setPort(Integer.parseInt(cl.getOptionValue('p')));
        } else {
            config.setPort(SmtpConfig.DEFAULT_PORT);
        }
        if (cl.hasOption('m')) {
            config.setMechanism(cl.getOptionValue('m').toUpperCase());
        }
        if (cl.hasOption('u')) {
            config.setAuthenticationId(cl.getOptionValue('u'));
        }
        if (cl.hasOption('w')) {
            setPassword(cl.getOptionValue('w'));
        }
        if (cl.hasOption('r')) {
            config.setRealm(cl.getOptionValue('r'));
        }
        if (cl.hasOption('s')) {
            config.setSecurity(MailConfig.Security.SSL);
        }
        if (cl.hasOption('d')) {
            config.getLogger().setLevel(Log.Level.trace);
        }
        if (cl.hasOption('h')) {
            usage();
            System.exit(0);
        }

        String[] remaining = cl.getArgs();
        config.setHost(remaining.length > 0 ? remaining[0] : "localhost");
    }

    public static void main(String[] args) throws Exception {
        new SmtpClient().run(args);
    }

}

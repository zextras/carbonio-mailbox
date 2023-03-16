// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.server;

import com.zextras.mailbox.metric.Metrics;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.imap.ImapConfig;
import com.zimbra.cs.imap.ImapServer;
import com.zimbra.cs.imap.NioImapServer;
import com.zimbra.cs.imap.TcpImapServer;
import com.zimbra.cs.lmtpserver.LmtpConfig;
import com.zimbra.cs.lmtpserver.LmtpServer;
import com.zimbra.cs.lmtpserver.TcpLmtpServer;
import com.zimbra.cs.milter.MilterConfig;
import com.zimbra.cs.milter.MilterServer;
import com.zimbra.cs.pop3.NioPop3Server;
import com.zimbra.cs.pop3.Pop3Config;
import com.zimbra.cs.pop3.Pop3Server;
import com.zimbra.cs.pop3.TcpPop3Server;
import com.zimbra.cs.util.ZimbraApplication;

public final class ServerManager {
    private LmtpServer lmtpServer;
    private Pop3Server pop3Server;
    private Pop3Server pop3SSLServer;
    private ImapServer imapServer;
    private ImapServer imapSSLServer;
    private MilterServer milterServer;

    private static final ServerManager INSTANCE = new ServerManager();

    // For debugging...
    private static final boolean NIO_ENABLED = Boolean.getBoolean("ZimbraNioEnabled");

    public static ServerManager getInstance() {
        return INSTANCE;
    }

    public void startServers() throws ServiceException {
        ZimbraApplication app = ZimbraApplication.getInstance();
        if (app.supports(LmtpServer.class)) {
        	lmtpServer = startLmtpServer();
        }
        if (app.supports(Pop3Server.class)) {
            if (isEnabled(Provisioning.A_zimbraPop3ServerEnabled)) {
                pop3Server = startPop3Server(false);
            }
            if (isEnabled(Provisioning.A_zimbraPop3SSLServerEnabled)) {
                pop3SSLServer = startPop3Server(true);
            }
        }
        if (app.supports(ImapServer.class)) {
            if (isEnabled(Provisioning.A_zimbraImapServerEnabled)) {
                imapServer = startImapServer(false);
            }
            if (isEnabled(Provisioning.A_zimbraImapSSLServerEnabled)) {
                imapSSLServer = startImapServer(true);
            }
        }

        // run milter service in the same process as mailtoxd. should be used only in dev environment
        if (app.supports(MilterServer.class)) {
            if (LC.milter_in_process_mode.booleanValue()) {
                milterServer = startMilterServer();
            }
        }
    }

    private static boolean isEnabled(String key) throws ServiceException {
        return Provisioning.getInstance().getLocalServer().getBooleanAttr(key, false);
    }

    private LmtpServer startLmtpServer() throws ServiceException {
        LmtpConfig config = LmtpConfig.getInstance();
        LmtpServer server = new TcpLmtpServer(config);
        server.start();
        return server;
    }

    private Pop3Server startPop3Server(boolean ssl) throws ServiceException {
        Pop3Config config = new Pop3Config(ssl);
        Pop3Server server = NIO_ENABLED || LC.nio_pop3_enabled.booleanValue() ?
            new NioPop3Server(config, Metrics.METER_REGISTRY) : new TcpPop3Server(config, Metrics.METER_REGISTRY);
        server.start();
        return server;
    }

    private ImapServer startImapServer(boolean ssl) throws ServiceException {
        ImapConfig config = new ImapConfig(ssl);
        ImapServer server = NIO_ENABLED || LC.nio_imap_enabled.booleanValue() ?
            new NioImapServer(config, Metrics.METER_REGISTRY) : new TcpImapServer(config, Metrics.METER_REGISTRY);
        server.start();
        return server;
    }

    private MilterServer startMilterServer() throws ServiceException {
        MilterServer server = new MilterServer(new MilterConfig());
        server.start();
        return server;
    }

    public void stopServers() throws ServiceException {
        if (lmtpServer != null) {
            lmtpServer.stop();
        }
        if (pop3Server != null) {
            pop3Server.stop();
        }
        if (pop3SSLServer != null) {
            pop3SSLServer.stop();
        }
        if (imapServer != null) {
            imapServer.stop();
        }
        if (imapSSLServer != null) {
            imapSSLServer.stop();
        }
        if (milterServer != null) {
            milterServer.stop();
        }
    }

    public LmtpServer getLmtpServer() {
        return lmtpServer;
    }

}

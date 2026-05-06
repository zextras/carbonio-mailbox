// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.milter;

import com.zextras.carbonio.systemd.SystemdNotify;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.PermissionCache;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.server.ProtocolHandler;
import com.zimbra.cs.server.TcpServer;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public final class TcpMilterServer extends TcpServer {

    public TcpMilterServer(MilterConfig config) throws ServiceException {
        super(config);
    }

    @Override
    public String getName() {
        return "MilterServer";
    }

    @Override
    protected ProtocolHandler newProtocolHandler() {
        return new TcpMilterHandler(this);
    }

    @Override
    public MilterConfig getConfig() {
        return (MilterConfig) super.getConfig();
    }

    private static final class MilterShutdownHook extends Thread {
        private final TcpMilterServer server;

        MilterShutdownHook(TcpMilterServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            ZimbraLog.milter.info("Shutting down milter server");
            server.stop();
        }
    }

    public static void main(String[] args) {
        try {
            Provisioning prov = Provisioning.getInstance();
            if (prov instanceof LdapProv) {
                ((LdapProv) prov).waitForLdapServer();
            }

            MilterConfig config = new MilterConfig();
            TcpMilterServer server = new TcpMilterServer(config);

            ClearCacheSignalHandler.register();

            MilterShutdownHook shutdownHook = new MilterShutdownHook(server);
            Runtime.getRuntime().addShutdownHook(shutdownHook);

            ZimbraLog.milter.info("Starting milter server");
            server.start();
            SystemdNotify.ready("milter ready");
        } catch (ServiceException e) {
            ZimbraLog.milter.error("Unable to start milter server", e);
        }
    }

    private static final class ClearCacheSignalHandler implements SignalHandler {
        @Override
        public void handle(Signal signal) {
            ZimbraLog.milter.info("Received Signal: %s", signal.getName());
            ZimbraLog.milter.info("Begin ACL cache invalidation");
            PermissionCache.invalidateAllCache();
            ZimbraLog.milter.info("ACL cache successfully cleared");
        }

        public static void register() {
            try {
                Signal hup = new Signal("CONT");
                ClearCacheSignalHandler handler = new ClearCacheSignalHandler();
                Signal.handle(hup, handler);
                ZimbraLog.milter.info("Registered signal handler: %s(%d)", hup.getName(), hup.getNumber());
            } catch (Throwable t) {
                ZimbraLog.milter.error("Unable to register signal handler CONT/19 and script refresh will not work", t);
            }
        }
    }
}

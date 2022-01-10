// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.iochannel;

import java.util.Collection;
import java.util.HashSet;

import com.google.common.collect.ImmutableSet;
import com.zimbra.common.iochannel.Config;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;

public class ZcsConfig extends Config {

    public ZcsConfig() throws ServiceException {
        Provisioning prov = Provisioning.getInstance();
        Server localServer = prov.getLocalServer();
        peerServers = new HashSet<ServerConfig>();
        // null on hostname in order to bind to all the addresses
        localConfig = new ServerConfig(localServer.getServiceHostname(), null, localServer.getMessageChannelPort());
        for (Server s : prov.getAllServers()) {
            if (!s.isLocalServer() && s.hasMailboxService() && s.isMessageChannelEnabled()) {
                peerServers.add(new ZcsServerConfig(s));
            }
        }
    }

    @Override
    public ServerConfig getLocalConfig() {
        return localConfig;
    }

    @Override
    public Collection<ServerConfig> getPeerServers() {
        return ImmutableSet.copyOf(peerServers);
    }

    private final ServerConfig localConfig;
    private final HashSet<ServerConfig> peerServers;

    private static final class ZcsServerConfig extends Config.ServerConfig {
        public ZcsServerConfig(Server s) {
            super(s.getServiceHostname(), s.getServiceHostname(), s.getMessageChannelPort());
        }
    }
}

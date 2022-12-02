// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.iochannel;

import java.util.Collection;

/**
 * Configuration data used in iochannel.
 *
 * @author jylee
 *
 */
public abstract class Config {

    public static class ServerConfig {
        public ServerConfig(String id, String host, int port) { this.id = id; this.host = host; this.port = port; }
        public final String id;
        public final String host;
        public final int port;
    }

    public abstract ServerConfig getLocalConfig();
    public abstract Collection<ServerConfig> getPeerServers();
}

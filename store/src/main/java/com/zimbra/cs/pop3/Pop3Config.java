// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.pop3;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.server.ServerConfig;
import com.zimbra.cs.util.BuildInfo;
import com.zimbra.cs.util.Config;

import static com.zimbra.cs.account.Provisioning.*;

public class Pop3Config extends ServerConfig {
    private static final String PROTOCOL = "POP3";

    public Pop3Config(boolean ssl) {
        super(PROTOCOL, ssl);
    }

    @Override
    public String getServerName() {
        return getAttr(A_zimbraPop3AdvertisedName, LC.zimbra_server_hostname.value());
    }

    @Override
    public String getServerVersion() {
        return getBooleanAttr(A_zimbraPop3ExposeVersionOnBanner, false) ?
            BuildInfo.VERSION : null;
    }

    @Override
    public String getBindAddress() {
        return getAttr(isSslEnabled() ?
            A_zimbraPop3SSLBindAddress : A_zimbraPop3BindAddress, null);
    }

    @Override
    public int getBindPort() {
        return isSslEnabled() ?
            getIntAttr(A_zimbraPop3SSLBindPort, Config.D_POP3_SSL_BIND_PORT) :
            getIntAttr(A_zimbraPop3BindPort, Config.D_POP3_BIND_PORT);
    }

    @Override
    public int getShutdownTimeout() {
       return getIntAttr(A_zimbraPop3ShutdownGraceSeconds, super.getShutdownTimeout());
    }

    @Override
    public int getMaxIdleTime() {
        return LC.pop3_max_idle_time.intValue();
    }

    @Override
    public int getMaxThreads() {
        return getIntAttr(A_zimbraPop3NumThreads, super.getMaxThreads());
    }

    @Override
    public int getThreadKeepAliveTime() {
        return LC.pop3_thread_keep_alive_time.intValue();
    }

    @Override
    public int getMaxConnections() {
        return getIntAttr(A_zimbraPop3MaxConnections, super.getMaxConnections());
    }

    @Override
    public int getWriteTimeout() {
        return LC.pop3_write_timeout.intValue();
    }

    @Override
    public Log getLog() {
        return ZimbraLog.pop;
    }

    @Override
    public String getConnectionRejected() {
        return "-ERR " + getDescription() + " closing connection; service busy";
    }

    public boolean isCleartextLoginsEnabled() {
        return getBooleanAttr(A_zimbraPop3CleartextLoginEnabled, false);
    }

    public boolean isSaslGssapiEnabled() {
        return getBooleanAttr(A_zimbraPop3SaslGssapiEnabled, false);
    }
}

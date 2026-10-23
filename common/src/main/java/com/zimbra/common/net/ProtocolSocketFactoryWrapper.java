// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.SocketFactory;

import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

class ProtocolSocketFactoryWrapper implements ConnectionSocketFactory {

    private final SocketFactory factory;

    ProtocolSocketFactoryWrapper(SocketFactory factory) {
        this.factory = factory;
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        return factory.createSocket();
    }

    @Override
    public Socket connectSocket(TimeValue connectTimeout, Socket sock, HttpHost host,
        InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context)
        throws IOException {
        Socket socket = sock != null ? sock : createSocket(context);
        if (localAddress != null && !socket.isBound()) {
            socket.bind(localAddress);
        }
        socket.connect(remoteAddress, connectTimeout == null ? 0 : connectTimeout.toMillisecondsIntBound());
        return socket;
    }
}

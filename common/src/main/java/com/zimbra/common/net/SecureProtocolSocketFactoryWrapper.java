// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

class SecureProtocolSocketFactoryWrapper extends ProtocolSocketFactoryWrapper
implements LayeredConnectionSocketFactory {

    private final SSLSocketFactory factory;

    SecureProtocolSocketFactoryWrapper(SSLSocketFactory factory) {
        super(factory);
        this.factory = factory;
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return factory.createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        return new Socket();
    }

    @Override
    public Socket connectSocket(TimeValue connectTimeout, Socket sock, HttpHost host,
        InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context)
        throws IOException {
        Socket plain = super.connectSocket(connectTimeout, sock, host, remoteAddress, localAddress, context);
        return createLayeredSocket(plain, host.getHostName(), remoteAddress.getPort(), context);
    }

    @Override
    public Socket createLayeredSocket(Socket socket, String target, int port, HttpContext context)
        throws IOException {
        if (socket == null) {
            return factory.createSocket(target, port);
        }
        return factory.createSocket(socket, target, port, true);
    }
}

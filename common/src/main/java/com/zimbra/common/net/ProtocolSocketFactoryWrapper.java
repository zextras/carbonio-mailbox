// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.apache.http.HttpHost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

class ProtocolSocketFactoryWrapper implements ConnectionSocketFactory {

    private final SocketFactory factory;

    ProtocolSocketFactoryWrapper(SocketFactory factory) {
        this.factory = factory;
    }
    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.http.conn.socket.ConnectionSocketFactory#createSocket(org.
     * apache.http.protocol.HttpContext)
     */
    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        if (context != null) {
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpHost  host = clientContext.getTargetHost();
            return createSocketFromHostInfo(host);
        }
        return factory.createSocket();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.http.conn.socket.ConnectionSocketFactory#connectSocket(int,
     * java.net.Socket, org.apache.http.HttpHost, java.net.InetSocketAddress,
     * java.net.InetSocketAddress, org.apache.http.protocol.HttpContext)
     */
    @Override
    public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host,
        InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context)
        throws IOException {
        int timeout = 0;
        if (context != null) {
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            timeout = clientContext.getConnection().getSocketTimeout();
        }


        if (timeout > 0) {
            if (sock != null && !sock.isBound()) {
                sock.bind(localAddress);
                sock.connect(remoteAddress, timeout);
            }
            return sock;
        } else {
            sock.connect(remoteAddress, connectTimeout);
            return sock;
        }
    }

    /**
     * @param host
     * @return
     * @throws IOException
     * @throws UnknownHostException
     */
    public Socket createSocketFromHostInfo(HttpHost host) throws IOException, UnknownHostException {
        if(host.getPort() == -1) {
            if (host.getSchemeName().equalsIgnoreCase("http")) {
               return  factory.createSocket(host.getHostName(), 80);
            } else if (host.getSchemeName().equalsIgnoreCase("https")) {
               return  factory.createSocket(host.getHostName(), 443);
            } else {
                throw new IOException("Unknown scheme for connecting  to host, Received  +  host.toHostString()");
            }
        } else {
           return  factory.createSocket(host.getHostName(), host.getPort());
        }
    }

}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import javax.net.SocketFactory;

class ProxySelectorSocketFactory extends SocketFactory {
  private final ProxySelector proxySelector;

  ProxySelectorSocketFactory(ProxySelector ps) {
    proxySelector = ps;
  }

  ProxySelectorSocketFactory() {
    this(null);
  }

  @Override
  public Socket createSocket() throws IOException {
    return new ProxySelectorSocket(
        proxySelector != null ? proxySelector : ProxySelectors.defaultProxySelector());
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return createSocket(new InetSocketAddress(host, port), null);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return createSocket(
        new InetSocketAddress(host, port), new InetSocketAddress(localAddress, localPort));
  }

  @Override
  public Socket createSocket(InetAddress address, int port) throws IOException {
    return createSocket(new InetSocketAddress(address, port), null);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return createSocket(
        new InetSocketAddress(address, port), new InetSocketAddress(localAddress, localPort));
  }

  private Socket createSocket(SocketAddress endpoint, SocketAddress bindpoint) throws IOException {
    Socket sock = createSocket();
    if (bindpoint != null) {
      sock.bind(bindpoint);
    }
    if (endpoint != null) {
      sock.connect(endpoint);
    }
    return sock;
  }
}

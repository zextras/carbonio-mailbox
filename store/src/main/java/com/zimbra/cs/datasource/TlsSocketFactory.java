// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource;

import com.zimbra.common.net.SocketFactories;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/*
 * Special SSLSocketFactory implementation to support JavaMail TLS. Since
 * JavaMail only allows the configuration of one socket factory we need this
 * in order to implement basic socket factory operations using plain socket
 * (before TLS negotiation) but then delegate to an SSLSocketFactory instance
 * when wrapping an existing socket (after TLS negotiation).
 */
public class TlsSocketFactory extends SSLSocketFactory {
  private final SocketFactory factory;
  private final SSLSocketFactory sslFactory;

  private static final TlsSocketFactory THE_ONE = new TlsSocketFactory();

  public TlsSocketFactory() {
    factory = SocketFactories.defaultSocketFactory();
    sslFactory = SocketFactories.defaultSSLSocketFactory();
  }

  public static TlsSocketFactory getInstance() {
    return THE_ONE;
  }

  public Socket createSocket() throws IOException {
    return factory.createSocket();
  }

  public Socket createSocket(InetAddress address, int port) throws IOException {
    return factory.createSocket(address, port);
  }

  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return factory.createSocket(address, port, localAddress, localPort);
  }

  public Socket createSocket(String host, int port) throws IOException {
    return factory.createSocket(host, port);
  }

  public Socket createSocket(String host, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return factory.createSocket(host, port, localAddress, localPort);
  }

  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
      throws IOException {
    return sslFactory.createSocket(s, host, port, autoClose);
  }

  public String[] getDefaultCipherSuites() {
    return sslFactory.getDefaultCipherSuites();
  }

  public String[] getSupportedCipherSuites() {
    return sslFactory.getSupportedCipherSuites();
  }
}

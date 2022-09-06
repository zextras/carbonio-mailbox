// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/** Override SSLSocketFactory to provide a createSocket() interface */
class CustomSSLSocketFactory extends SSLSocketFactory {
  private final SSLSocketFactory sslFactory;
  private final SSLSocket sampleSSLSocket; // Sample socket for obtaining default SSL params
  private final SocketFactory factory; // Optional SocketFactory
  private final boolean verifyHostname;

  CustomSSLSocketFactory(TrustManager tm, SocketFactory sf, boolean verifyHostname)
      throws GeneralSecurityException, IOException {

    SSLContext context = SSLContext.getInstance("TLS");
    context.init(null, tm != null ? new TrustManager[] {tm} : null, null);
    sslFactory = context.getSocketFactory();
    sampleSSLSocket = (SSLSocket) sslFactory.createSocket();
    factory = sf;
    this.verifyHostname = verifyHostname && tm instanceof CustomTrustManager;
  }

  boolean isVerifyHostname() {
    return verifyHostname;
  }

  SSLSocket getSampleSSLSocket() {
    return sampleSSLSocket;
  }

  SSLSocket wrap(Socket socket) throws IOException {
    return (SSLSocket)
        sslFactory.createSocket(
            socket, socket.getInetAddress().getHostName(), socket.getPort(), true);
  }

  @Override
  public Socket createSocket() throws IOException {
    if (factory != null) {
      return new CustomSSLSocket(this, factory.createSocket());
    } else {
      return new CustomSSLSocket(this, (SSLSocket) sslFactory.createSocket(), null);
    }
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

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return createSocket(new InetSocketAddress(host, port), null);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException {
    return createSocket(
        new InetSocketAddress(host, port), new InetSocketAddress(localHost, localPort));
  }

  @Override
  public Socket createSocket(Socket socket, String host, int port, boolean flag)
      throws IOException {
    return new CustomSSLSocket(
        this, (SSLSocket) sslFactory.createSocket(socket, host, port, flag), host);
  }

  private Socket createSocket(InetSocketAddress endpoint, InetSocketAddress bindpoint)
      throws IOException {
    Socket sock = createSocket();
    if (bindpoint != null) {
      sock.bind(bindpoint);
    }
    if (endpoint != null) {
      sock.connect(endpoint);
    }
    return sock;
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return sslFactory.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return sslFactory.getSupportedCipherSuites();
  }
}

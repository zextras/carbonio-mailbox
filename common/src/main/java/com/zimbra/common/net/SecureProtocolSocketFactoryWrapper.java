// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.net;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.HttpHost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

class SecureProtocolSocketFactoryWrapper extends ProtocolSocketFactoryWrapper
    implements LayeredConnectionSocketFactory {

  private SSLSocketFactory factory;

  SecureProtocolSocketFactoryWrapper(SSLSocketFactory factory) {
    super(factory);
    this.factory = factory;
  }

  public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
      throws IOException {
    return factory.createSocket(socket, host, port, autoClose);
  }

  /* (non-Javadoc)
   * @see org.apache.http.conn.socket.ConnectionSocketFactory#createSocket(org.apache.http.protocol.HttpContext)
   */
  @Override
  public Socket createSocket(HttpContext context) throws IOException {
    if (context != null) {
      HttpClientContext clientContext = HttpClientContext.adapt(context);
      HttpHost host = clientContext.getTargetHost();
      return createSocketFromHostInfo(host);
    }
    return factory.createSocket();
  }

  /* (non-Javadoc)
   * @see org.apache.http.conn.socket.LayeredConnectionSocketFactory#createLayeredSocket(java.net.Socket, java.lang.String, int, org.apache.http.protocol.HttpContext)
   */
  @Override
  public Socket createLayeredSocket(Socket socket, String target, int port, HttpContext context)
      throws IOException, UnknownHostException {
    if (socket == null) {
      if (context != null) {
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpHost host = clientContext.getTargetHost();
        return createSocketFromHostInfo(host);
      } else {
        return factory.createSocket(target, port);
      }
    } else {
      return factory.createSocket(socket, target, port, true);
    }
  }
}

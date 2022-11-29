// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package org.apache.mina.filter.ssl;
import javax.net.ssl.SSLContext;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;

/** classpath visibility workaround for access into SSL handshake status **/
public class ZimbraSslFilter extends SslFilter {

    private static final AttributeKey SSL_HANDLER_KEY = new AttributeKey(SslFilter.class, "handler");

    public ZimbraSslFilter(SSLContext sslContext) {
        super(sslContext);
    }

    public boolean isSslHandshakeComplete(IoSession session) {
        SslHandler handler = getSslSessionHandler(session);
        return handler != null && handler.isHandshakeComplete();
    }

    private SslHandler getSslSessionHandler(IoSession session) {
        SslHandler handler = (SslHandler) session.getAttribute(SSL_HANDLER_KEY);

        if (handler == null) {
            throw new IllegalStateException();
        }

        if (handler.getSslFilter() != this) {
            throw new IllegalArgumentException("Not managed by this filter.");
        }

        return handler;
    }
}

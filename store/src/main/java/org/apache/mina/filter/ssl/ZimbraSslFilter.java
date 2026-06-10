// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package org.apache.mina.filter.ssl;
import javax.net.ssl.SSLContext;

import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

/** classpath visibility workaround for access into SSL handshake status **/
public class ZimbraSslFilter extends SslFilter {

    public static final AttributeKey DISABLE_ENCRYPTION_ONCE = new AttributeKey(ZimbraSslFilter.class, "disableEncryptionOnce");

    public ZimbraSslFilter(SSLContext sslContext) {
        super(sslContext);
    }

    @Override
    public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if (session.getAttribute(DISABLE_ENCRYPTION_ONCE) != null) {
            session.removeAttribute(DISABLE_ENCRYPTION_ONCE);
            nextFilter.filterWrite(session, writeRequest);
            return;
        }
        super.filterWrite(nextFilter, session, writeRequest);
    }

    public boolean isSslHandshakeComplete(IoSession session) {
        SslHandler handler = (SslHandler) session.getAttribute(SslFilter.SSL_HANDLER);
        return handler != null && handler.isConnected();
    }
}

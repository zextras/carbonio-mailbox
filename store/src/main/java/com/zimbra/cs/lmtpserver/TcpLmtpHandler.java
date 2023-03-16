// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.lmtpserver;

import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.zimbra.common.io.TcpServerInputStream;
import com.zimbra.common.util.NetUtil;
import com.zimbra.common.util.ZimbraLog;

public class TcpLmtpHandler extends LmtpHandler {
    private TcpServerInputStream inputStream;

    TcpLmtpHandler(LmtpServer server, MeterRegistry meterRegistry) {
        super(server, meterRegistry);
    }

    @Override
    protected boolean setupConnection(Socket connection) throws IOException {
        reset();
        inputStream = new TcpServerInputStream(connection.getInputStream());
        mWriter = new LmtpWriter(connection.getOutputStream());
        return setupConnection(connection.getInetAddress());
    }

    @Override
    protected synchronized void dropConnection() {
        ZimbraLog.addIpToContext(mRemoteAddress);
        try {
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
            if (mWriter != null) {
                mWriter.close();
                mWriter = null;
            }
        } catch (IOException e) {
            if (ZimbraLog.lmtp.isDebugEnabled()) {
                ZimbraLog.lmtp.info("I/O error while closing connection", e);
            } else {
                ZimbraLog.lmtp.info("I/O error while closing connection: " + e);
            }
        } finally {
            ZimbraLog.clearContext();
        }
    }

    @Override
    protected boolean processCommand() throws IOException {
        // make sure that the connection wasn't dropped during a preceding command processing
        if (inputStream != null)
            return processCommand(inputStream.readLine());
        return false;
    }

    @Override
    protected void continueDATA() throws IOException {
        LmtpMessageInputStream min = new LmtpMessageInputStream(inputStream, getAdditionalHeaders());
        processMessageData(min);
    }
    
    @Override
        protected void doSTARTTLS(String arg) throws IOException {
            if (arg != null) {
            	sendReply(LmtpReply.STARTTLS_WITH_PARAMETER); // parameter supplied to STARTTLS
                return;
            }
            sendReply(LmtpReply.READY_TO_START_TLS);
            SSLSocketFactory fac = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket tlsconn = (SSLSocket) fac.createSocket(connection, connection.getInetAddress().getHostName(),
                        connection.getPort(), true);
            NetUtil.setSSLEnabledCipherSuites(tlsconn, config.getSslExcludedCiphers(), config.getSslIncludedCiphers());
            tlsconn.setUseClientMode(false);
            startHandshake(tlsconn);
            inputStream = new TcpServerInputStream(tlsconn.getInputStream());
            mWriter = new LmtpWriter(tlsconn.getOutputStream());
            startedTLS = true;
    }

}

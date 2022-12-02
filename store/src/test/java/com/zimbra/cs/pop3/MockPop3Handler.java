// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.pop3;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.google.common.base.Charsets;

class MockPop3Handler extends Pop3Handler {

    MockPop3Handler() {
        super(new Pop3Config(false));
        output = new ByteArrayOutputStream();
    }

    @Override
    void startTLS() throws IOException {
    }

    @Override
    void completeAuthentication() throws IOException {
    }

    @Override
    InetSocketAddress getLocalAddress() {
        return new InetSocketAddress("localhost", 0);
    }

    @Override
    void sendLine(String line, boolean flush) throws IOException {
    }

}

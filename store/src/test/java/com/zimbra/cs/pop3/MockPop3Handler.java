// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.pop3;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.commons.io.output.ByteArrayOutputStream;

class MockPop3Handler extends Pop3Handler {

  MockPop3Handler() {
    super(new Pop3Config(false), new SimpleMeterRegistry());
    output = new ByteArrayOutputStream();
  }

  @Override
  void startTLS() throws IOException {}

  @Override
  void completeAuthentication() throws IOException {}

  @Override
  InetSocketAddress getLocalAddress() {
    return new InetSocketAddress("localhost", 0);
  }

  @Override
  void sendLine(String line, boolean flush) throws IOException {}
}

// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.imap;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.commons.io.output.ByteArrayOutputStream;

class MockImapHandler extends ImapHandler {

  MockImapHandler() {
    super(new ImapConfig(false));
    output = new ByteArrayOutputStream();
  }

  @Override
  protected String getRemoteIp() {
    return "127.0.0.1";
  }

  @Override
  protected void sendLine(String line, boolean flush) throws IOException {
    output.write(line.getBytes(Charsets.UTF_8));
    output.write(LINE_SEPARATOR_BYTES);
  }

  @Override
  protected void dropConnection(boolean sendBanner) {}

  @Override
  protected void close() {}

  @Override
  protected void enableInactivityTimer() throws IOException {}

  @Override
  protected void completeAuthentication() throws IOException {}

  @Override
  protected boolean doSTARTTLS(String tag) throws IOException {
    return false;
  }

  @Override
  protected InetSocketAddress getLocalAddress() {
    return new InetSocketAddress("localhost", 0);
  }
}

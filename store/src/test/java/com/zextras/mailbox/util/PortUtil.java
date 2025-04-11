package com.zextras.mailbox.util;

import java.io.IOException;

public class PortUtil {
  public static int findFreePort() {
    try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException("Unable to find a free port", e);
    }
  }
}

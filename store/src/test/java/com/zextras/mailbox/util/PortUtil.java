package com.zextras.mailbox.util;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.zimbra.cs.db.HSQLDB;
import java.io.IOException;

/**
 * This utility class allows easy setup for the Mailbox environment using {@link #setUp()} method.
 * To clean up th environment remember to call {@link #tearDown()}. It uses an {@link
 * InMemoryDirectoryServer} and an in memory database using {@link HSQLDB} as dependencies.
 */
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

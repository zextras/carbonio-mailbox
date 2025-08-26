package com.zimbra.cert.util;

import java.io.IOException;

/**
 * {@link ProcessStarter} implementation for real OS using {@link ProcessBuilder}
 * @author davidefrison
 */
public class ProcessStarterProvider implements ProcessStarter {

  @Override
  public Process start(String ...args) {
    try {
      return new ProcessBuilder(args).start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

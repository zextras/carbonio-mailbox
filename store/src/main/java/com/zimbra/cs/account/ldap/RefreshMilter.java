package com.zimbra.cs.account.ldap;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class RefreshMilter {

  public static final String ZMMILTERCTL = "zmmilterctl";
  public static final String CMD_PATH = ("%s/bin/%s").formatted(LC.zimbra_home.value(), ZMMILTERCTL);

  public static RefreshMilter instance = new RefreshMilter();

  private RefreshMilter(){}

  public synchronized void refresh() throws IOException, InterruptedException {
    Process process = new ProcessBuilder(CMD_PATH, "refresh").start();
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      ZimbraLog.mailbox.warn("Error refreshing milter, exit code %s\nstdout:\n%s\nstderr\n%s".formatted(
              exitCode,
              readOutput(process.getInputStream()),
              readOutput(process.getErrorStream())
      ));
    }
  }

  private static String readOutput(InputStream inputStream) throws IOException {
    try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
      return output.lines().collect(Collectors.joining("\n"));
    }
  }

}

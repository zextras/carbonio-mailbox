package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.provutil.TrackCommandRequestHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProvUtilCommandRunner {

  public record CommandOutput(String stdout, String stderr, List<String> requests) {
  }

  CommandOutput runCommandString(String command) throws ServiceException, IOException {
    String[] commandWithArgs = command.split("\s+");
    return runCommand(Arrays.asList(commandWithArgs));
  }

  CommandOutput runCommand(String... commandWithArgs) throws ServiceException, IOException {
    return runCommand(Arrays.asList(commandWithArgs));
  }

  CommandOutput runCommand(List<String> commandWithArgs) throws ServiceException, IOException {
//    TrackCommandRequestHandler.setCommand(commandWithArgs);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
    Thread hook = new Thread(() -> {
      if (outputStream != null) {
        System.out.println("stdout");
        System.out.println(outputStream.toString(StandardCharsets.UTF_8));
        System.out.println("stderr");
        System.out.println(errorStream.toString(StandardCharsets.UTF_8));
      }
    });
    Runtime.getRuntime().addShutdownHook(hook);
    try {
      ProvUtil.main(new Console(outputStream, errorStream), commandWithArgs.toArray(new String[] {}));
      Runtime.getRuntime().removeShutdownHook(hook);
      return new CommandOutput(
          outputStream.toString(StandardCharsets.UTF_8),
          errorStream.toString(StandardCharsets.UTF_8),
          new ArrayList<>(TrackCommandRequestHandler.getRequestString())
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      TrackCommandRequestHandler.reset();
    }
  }
}


class NoExitSecurityManager extends SecurityManager {

  @Override
  public void checkPermission(Permission perm)
  {
    /* Allow everything else. */
  }

  @Override
  public void checkExit(int status)
  {
    /* Don't allow exit with any status code. */
    throw new SecurityException();
  }

}
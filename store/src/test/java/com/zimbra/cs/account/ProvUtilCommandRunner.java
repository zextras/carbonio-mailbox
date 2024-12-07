package com.zimbra.cs.account;

import com.zimbra.cs.account.provutil.TrackCommandRequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProvUtilCommandRunner {

  private static final Logger log = LogManager.getLogger(ProvUtilCommandRunner.class);

  public record CommandOutput(String stdout, String stderr, List<String> requests) {
  }

  static CommandOutput runCommand(String... commandWithArgs) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
    Thread hook = new Thread(() -> {
      log.error("""
          Error running command
          
          {}
          
          # STDOUT
          
          {}
          # STDERR
          
          {}""",
              String.join(" ", commandWithArgs),
              outputStream.toString(StandardCharsets.UTF_8),
              errorStream.toString(StandardCharsets.UTF_8));
    });
    /** Install a shutdown hook that prints stdout and stderr if command calls System.exit()*/
    Runtime.getRuntime().addShutdownHook(hook);
    try {
      ProvUtil.main(new Console(outputStream, errorStream), commandWithArgs);
      /** Remove the 'prints stdout and stderr' shutdown hook if command completes successfully */
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
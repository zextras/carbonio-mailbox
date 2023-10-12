// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.net.SocketFactories;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import jline.ConsoleReader;
import jline.ConsoleReaderInputStream;
import jline.History;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

/** Cli utility class. */
public class CliUtil {
  private static final String DEFAULT_LOG_LEVEL = "INFO";

  private CliUtil() {
    throw new java.lang.UnsupportedOperationException("Utility class and cannot be instantiated");
  }

  /**
   * Sets up root logger level configuration property with {@link CliUtil#DEFAULT_LOG_LEVEL} if
   * System.getProperty("zimbra.log4j.level") is empty. See {@link CliUtil#toolSetup(String, String,
   * boolean)}
   */
  public static void toolSetup() {
    toolSetup(DEFAULT_LOG_LEVEL);
  }

  /**
   * Sets up root logger level configuration property with provided level if
   * System.getProperty("zimbra.log4j.level") is empty. See {@link CliUtil#toolSetup(String, String,
   * boolean)}
   *
   * @param logLevel log level to set up root logger with
   */
  public static void toolSetup(final String logLevel) {
    toolSetup(logLevel, null, false);
  }

  /**
   * Performs a tool setup. Sets up Log4j configuration properties with provided vales, registers
   * protocols for {@link SocketFactories}, disables HTTP soap client timeout (Bug: 47051).
   *
   * @param logLevel log level to set up root logger and optionally newly created logger (in case if
   *     logfile was specified)
   * @param logFile optionally specify a logging file
   * @param showThreads specify if handling threads are needed
   */
  public static void toolSetup(final String logLevel, final String logFile, boolean showThreads) {
    ZimbraLog.toolSetupLog4j(logLevel, logFile, showThreads);

    SocketFactories.registerProtocols();

    LC.httpclient_soaphttptransport_so_timeout.setDefault(
        LC.cli_httpclient_soaphttptransport_so_timeout.longValue());
  }

  /**
   * Sets up the default value in local configuration cache for
   * httpclient_soaphttptransport_so_timeout for CLI utilities. Use
   * cli_httpclient_soaphttptransport_so_timeout configuration key to override the default using
   * local configuration file.
   */
  public static void setCliSoapHttpTransportTimeout() {
    LC.httpclient_soaphttptransport_so_timeout.setDefault(
        LC.cli_httpclient_soaphttptransport_so_timeout.longValue());
  }

  /**
   * Looks up an <tt>Option</tt> by its short or long name. This workaround is necessary because
   * <tt>CommandLine.hasOption()</tt> doesn't support long option names.
   */
  public static Option getOption(CommandLine cl, String name) {
    for (Option opt : cl.getOptions()) {
      if (StringUtil.equal(opt.getOpt(), name) || StringUtil.equal(opt.getLongOpt(), name)) {
        return opt;
      }
    }
    return null;
  }

  /**
   * Returns <tt>true</tt> if either a short or long option with the given name was specified on the
   * command line.
   */
  public static boolean hasOption(CommandLine cl, String name) {
    return (getOption(cl, name) != null);
  }

  /**
   * Returns the value for the given option name.
   *
   * @param cl command line
   * @param name either short or long option name
   */
  public static String getOptionValue(CommandLine cl, String name) {
    Option opt = getOption(cl, name);
    if (opt == null) {
      return null;
    }
    return opt.getValue();
  }

  /**
   * Turns on command line editing with JLine.
   *
   * @param histFilePath path to the history file, or {@code null} to not save history
   * @throws IOException if the history file is not writable or cannot be created
   */
  public static void enableCommandLineEditing(String histFilePath) throws IOException {
    File histFile = null;
    if (histFilePath != null) {
      histFile = new File(histFilePath);
      if (!histFile.exists()) {
        if (!histFile.createNewFile()) {
          throw new IOException("Unable to create history file " + histFilePath);
        }
      }
      if (!histFile.canWrite()) {
        throw new IOException(histFilePath + " is not writable");
      }
    }
    ConsoleReader reader = new ConsoleReader();
    if (histFile != null) {
      reader.setHistory(new History(histFile));
    }
    ConsoleReaderInputStream.setIn(reader);
  }

  public static boolean confirm(String msg) {
    System.out.print(msg + " [Y]es, [N]o: ");
    BufferedReader in;
    try {
      in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
      String line = StringUtil.readLine(in);
      if ("y".equalsIgnoreCase(line) || "yes".equalsIgnoreCase(line)) {
        return true;
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
}

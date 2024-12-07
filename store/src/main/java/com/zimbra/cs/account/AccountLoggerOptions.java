package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;

/**
 * FIXME: move this class inside subpackage commands
 */
public class AccountLoggerOptions {
  /*
  FIXME make fields package visible once class is moved inside commands subpackage
   */
  public String server;
  public String[] args;

  /**
   * Handles an optional <tt>-s</tt> or <tt>--server</tt> argument that may be passed to the logging
   * commands. Returns an <tt>AccountLogggerOptions</tt> object that contains all arguments except
   * the server option and value.
   */
  public static AccountLoggerOptions parseAccountLoggerOptions(String[] args) throws ServiceException {
    AccountLoggerOptions alo = new AccountLoggerOptions();
    if (args.length > 1 && (args[1].equals("-s") || args[1].equals("--server"))) {
      if (args.length == 2) {
        throw ServiceException.FAILURE("Server name not specified.", null);
      }
      alo.server = args[2];

      int numArgs = args.length - 2;
      alo.args = new String[numArgs];
      alo.args[0] = args[0];
      System.arraycopy(args, 3, alo.args, 1, numArgs - 1);
    } else {
      alo.args = args;
    }
    return alo;
  }
}

package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.accesscontrol.Right;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.List;

class GetAllRightsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetAllRightsCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doGetAllRights(args);
  }

  private void doGetAllRights(String[] args) throws ServiceException, ArgException {
    boolean verbose = false;
    String targetType = null;
    String rightClass = null;

    int i = 1;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-v")) {
        verbose = true;
      } else if (arg.equals("-t")) {
        i++;
        if (i == args.length) {
          throw new ArgException("not enough arguments");
        } else {
          targetType = args[i];
        }
      } else if (arg.equals("-c")) {
        i++;
        if (i == args.length) {
          throw new ArgException("not enough arguments");
        } else {
          rightClass = args[i];
        }
      } else {
        throw new ArgException("invalid arg: " + arg);
      }
      i++;
    }

    List<Right> allRights = provUtil.getProvisioning().getAllRights(targetType, false, rightClass);
    for (Right right : allRights) {
      if (verbose) {
        dumpRight(right);
      } else {
        provUtil.getConsole().println(right.getName());
      }
    }
  }

  private void dumpRight(Right right) {
    dumper.dumpRight(right, true);
  }
}

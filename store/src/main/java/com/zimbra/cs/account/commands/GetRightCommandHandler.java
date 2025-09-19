package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.accesscontrol.Right;

class GetRightCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetRightCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doGetRight(args);
  }

  private void doGetRight(String[] args) throws ServiceException, ArgException {
    boolean expandComboRight = false;
    String right = args[1];
    if (args.length > 2) {
      if (args[2].equals("-e")) {
        expandComboRight = true;
      } else {
        throw new ArgException("invalid arguments");
      }
    }
    dumper.dumpRight(lookupRight(right), expandComboRight);
  }

  private Right lookupRight(String rightName) throws ServiceException {
    return provUtil.getProvisioning().getRight(rightName, false);
  }
}

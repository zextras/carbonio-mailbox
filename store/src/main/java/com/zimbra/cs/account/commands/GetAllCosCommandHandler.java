package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.ProvUtil;

import java.util.List;
import java.util.Set;

public class GetAllCosCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllCosCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doGetAllCos(args);
  }

  private void doGetAllCos(String[] args) throws ServiceException {
    boolean verbose = args.length > 1 && args[1].equals("-v");
    Set<String> attrNames = provUtil.getArgNameSet(args, verbose ? 2 : 1);
    List<Cos> allcos = provUtil.getProvisioning().getAllCos();
    for (Cos cos : allcos) {
      if (verbose) {
        provUtil.dumpCos(cos, attrNames);
      } else {
        provUtil.getConsole().println(cos.getName());
      }
    }
  }
}

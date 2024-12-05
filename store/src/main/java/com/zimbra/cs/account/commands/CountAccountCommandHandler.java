package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;

public class CountAccountCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CountAccountCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doCountAccount(args);
  }

  private void doCountAccount(String[] args) throws ServiceException {
    String domain = args[1];
    Domain d = provUtil.lookupDomain(domain);

    Provisioning.CountAccountResult result = provUtil.getProvisioning().countAccount(d);
    String formatHeading = "%-20s %-40s %s\n";
    String format = "%-20s %-40s %d\n";
    var console = provUtil.getConsole();
    console.print(String.format(formatHeading, "cos name", "cos id", "# of accounts"));
    console.print(String.format(
            formatHeading,
            "--------------------",
            "----------------------------------------",
            "--------------------"));
    for (Provisioning.CountAccountResult.CountAccountByCos c : result.getCountAccountByCos()) {
      console.print(String.format(format, c.getCosName(), c.getCosId(), c.getCount()));
    }
    console.println();
  }
}

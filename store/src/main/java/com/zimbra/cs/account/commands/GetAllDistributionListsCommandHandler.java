package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.UsageException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.ProvUtil;

import java.util.Collection;
import java.util.List;

class GetAllDistributionListsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetAllDistributionListsCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException, UsageException {
    doGetAllDistributionLists(args);
  }

  private void doGetAllDistributionLists(String[] args) throws ServiceException, UsageException {
    String d = null;
    boolean verbose = false;
    var console = provUtil.getConsole();
    int i = 1;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-v")) {
        verbose = true;
      } else {
        if (d == null) {
          d = arg;
        } else {
          console.println("invalid arg: " + arg + ", already specified domain: " + d);
          provUtil.usageWithUsageException();
          return;
        }
      }
      i++;
    }
    var prov = provUtil.getProvisioning();

    if (d == null) {
      List<Domain> domains = prov.getAllDomains();
      for (Domain domain : domains) {
        Collection<?> dls = prov.getAllGroups(domain);
        for (Object obj : dls) {
          Group dl = (Group) obj;
          if (verbose) {
            dumper.dumpGroup(dl, null);
          } else {
            console.println(dl.getName());
          }
        }
      }
    } else {
      Domain domain = provUtil.lookupDomain(d);
      Collection<?> dls = prov.getAllGroups(domain);
      for (Object obj : dls) {
        Group dl = (Group) obj;
        if (verbose) {
          dumper.dumpGroup(dl, null);
        } else {
          console.println(dl.getName());
        }
      }
    }
  }

}

package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.List;

class GetAllCalendarResourcesCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetAllCalendarResourcesCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetAllCalendarResources(args);
  }

  private void doGetAllCalendarResources(String[] args) throws ServiceException {
    boolean verbose = false;
    boolean applyDefault = true;
    String d = null;
    String s = null;

    var console = provUtil.getConsole();
    int i = 1;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-v")) {
        verbose = true;
      } else if (arg.equals("-e")) {
        applyDefault = false;
      } else if (arg.equals("-s")) {
        i++;
        if (i < args.length) {
          if (s == null) {
            s = args[i];
          } else {
            console.println("invalid arg: " + args[i] + ", already specified -s with " + s);
            provUtil.usage();
            return;
          }
        } else {
          provUtil.usage();
          return;
        }
      } else {
        if (d == null) {
          d = arg;
        } else {
          console.println("invalid arg: " + arg + ", already specified domain: " + d);
          provUtil.usage();
          return;
        }
      }
      i++;
    }

    if (!applyDefault && !verbose) {
      console.println(ProvUtil.ERR_INVALID_ARG_EV);
      provUtil.usage();
      return;
    }

    // always use LDAP
    Provisioning prov = Provisioning.getInstance();

    Server server = null;
    if (s != null) {
      server = provUtil.lookupServer(s);
    }
    if (d == null) {
      List<Domain> domains = prov.getAllDomains();
      for (Domain domain : domains) {
        doGetAllCalendarResources(prov, domain, server, verbose, applyDefault);
      }
    } else {
      Domain domain = provUtil.lookupDomain(d, prov);
      doGetAllCalendarResources(prov, domain, server, verbose, applyDefault);
    }
  }

  private void doGetAllCalendarResources(
          Provisioning prov,
          Domain domain,
          Server server,
          final boolean verbose,
          final boolean applyDefault)
          throws ServiceException {
    NamedEntry.Visitor visitor =
            entry -> {
              if (verbose) {
                dumper.dumpCalendarResource((CalendarResource) entry, applyDefault, null);
              } else {
                provUtil.getConsole().println(entry.getName());
              }
            };
    prov.getAllCalendarResources(domain, server, visitor);
  }

}

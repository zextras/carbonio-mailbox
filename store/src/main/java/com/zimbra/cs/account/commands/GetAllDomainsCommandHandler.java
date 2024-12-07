package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;

import java.util.List;
import java.util.Set;

public class GetAllDomainsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllDomainsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doGetAllDomains(args);
  }

  private void doGetAllDomains(String[] args) throws ServiceException {
    boolean verbose = false;
    boolean applyDefault = true;

    int i = 1;
    while (i < args.length) {
      String arg = args[i];
      if (arg.equals("-v")) {
        verbose = true;
      } else if (arg.equals("-e")) {
        applyDefault = false;
      } else {
        break;
      }
      i++;
    }

    var console = provUtil.getConsole();
    if (!applyDefault && !verbose) {
      console.println(ProvUtil.ERR_INVALID_ARG_EV);
      provUtil.usage();
      return;
    }

    Set<String> attrNames = provUtil.getArgNameSet(args, i);

    List<Domain> domains;
    if (provUtil.getProvisioning() instanceof SoapProvisioning soapProv) {
      domains = soapProv.getAllDomains(applyDefault);
    } else {
      domains = provUtil.getProvisioning().getAllDomains();
    }
    for (Domain domain : domains) {
      if (verbose) {
        provUtil.dumpDomain(domain, attrNames);
      } else {
        console.println(domain.getName());
      }
    }
  }


}

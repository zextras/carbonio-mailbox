package com.zimbra.cs.account.commands;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import org.apache.http.HttpException;

import java.io.IOException;

class GetDomainInfoCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetDomainInfoCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetDomainInfo(args);
  }

  private void doGetDomainInfo(String[] args) throws ServiceException {
    if (!(provUtil.getProvisioning() instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) provUtil.getProvisioning();
    Key.DomainBy by = Key.DomainBy.fromString(args[1]);
    String key = args[2];
    Domain domain = sp.getDomainInfo(by, key);
    if (domain == null) {
      throw AccountServiceException.NO_SUCH_DOMAIN(key);
    } else {
      provUtil.dumpDomain(domain, provUtil.getArgNameSet(args, 3));
    }
  }
}

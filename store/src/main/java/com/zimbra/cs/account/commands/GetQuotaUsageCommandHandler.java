package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.List;

class GetQuotaUsageCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetQuotaUsageCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetQuotaUsage(args);
 }

  private void doGetQuotaUsage(String[] args) throws ServiceException {
    var prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    List<SoapProvisioning.QuotaUsage> result = sp.getQuotaUsage(args[1]);
    for (SoapProvisioning.QuotaUsage u : result) {
      provUtil.getConsole().println(String.format("%s %d %d", u.getName(), u.getLimit(), u.getUsed()));
    }
  }
}

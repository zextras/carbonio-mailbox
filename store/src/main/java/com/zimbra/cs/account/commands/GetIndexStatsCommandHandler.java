package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import org.apache.http.HttpException;

import java.io.IOException;

class GetIndexStatsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetIndexStatsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetIndexStats(args);
  }

  private void doGetIndexStats(String[] args) throws ServiceException {
    var prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account acct = provUtil.lookupAccount(args[1]);
    SoapProvisioning.IndexStatsInfo stats = sp.getIndexStats(acct);
    provUtil.getConsole().println(String.format(
            "stats: maxDocs:%d numDeletedDocs:%d", stats.getMaxDocs(), stats.getNumDeletedDocs()));
  }
}

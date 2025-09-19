package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;

class RecalculateMailboxCountsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public RecalculateMailboxCountsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doRecalculateMailboxCounts(args);
  }

  private void doRecalculateMailboxCounts(String[] args) throws ServiceException {
    var prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account account = provUtil.lookupAccount(args[1]);
    long quotaUsed = sp.recalculateMailboxCounts(account);
    provUtil.getConsole().print("account: " + account.getName() + "\nquotaUsed: " + quotaUsed + "\n");
  }
}

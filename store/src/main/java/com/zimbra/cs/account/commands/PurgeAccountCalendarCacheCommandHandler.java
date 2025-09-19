package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;

class PurgeAccountCalendarCacheCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public PurgeAccountCalendarCacheCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doPurgeAccountCalendarCache(args);
  }

  private void doPurgeAccountCalendarCache(String[] args) throws ServiceException {
    var prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    if (args.length > 1) {
      for (int i = 1; i < args.length; i++) {
        Account acct = provUtil.lookupAccount(args[i], true);
        prov.purgeAccountCalendarCache(acct.getId());
      }
    }
  }
}

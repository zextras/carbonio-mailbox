package com.zimbra.cs.account.commands;

import static com.zextras.mailbox.quota.QuotaUsageMessages.USAGE_SCOPE_NOTE;

import com.zextras.mailbox.util.ByteSizeFormatter;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import org.apache.http.HttpException;

import java.io.IOException;

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
    provUtil
        .getConsole()
        .print(
            USAGE_SCOPE_NOTE
                + "\naccount: "
                + account.getName()
                + "\nquotaUsed: "
                + ByteSizeFormatter.format(quotaUsed)
                + "\n"
                + USAGE_SCOPE_NOTE
                + "\n");
  }
}

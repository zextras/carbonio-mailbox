package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import org.apache.http.HttpException;

import java.io.IOException;

class GetMailboxInfoCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetMailboxInfoCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetMailboxInfo(args);
  }

  private void doGetMailboxInfo(String[] args) throws ServiceException {
    var prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account acct = provUtil.lookupAccount(args[1]);
    SoapProvisioning.MailboxInfo info = sp.getMailbox(acct);
    provUtil.getConsole().println(String.format("mailboxId: %s%nquotaUsed: %d", info.getMailboxId(), info.getUsed()));
  }
}

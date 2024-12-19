package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import org.apache.http.HttpException;

import java.io.IOException;

class ReIndexMailboxCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public ReIndexMailboxCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doReIndexMailbox(args);
  }

  private void doReIndexMailbox(String[] args) throws ServiceException {
    var prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    SoapProvisioning sp = (SoapProvisioning) prov;
    Account acct = provUtil.lookupAccount(args[1]);
    SoapProvisioning.ReIndexBy by = null;
    String[] values = null;
    if (args.length > 3) {
      try {
        by = SoapProvisioning.ReIndexBy.valueOf(args[3]);
      } catch (IllegalArgumentException e) {
        throw ServiceException.INVALID_REQUEST("invalid reindex-by", null);
      }
      if (args.length > 4) {
        values = new String[args.length - 4];
        System.arraycopy(args, 4, values, 0, args.length - 4);
      } else {
        throw ServiceException.INVALID_REQUEST("missing reindex-by values", null);
      }
    }
    SoapProvisioning.ReIndexInfo info = sp.reIndex(acct, args[2], by, values);
    SoapProvisioning.ReIndexInfo.Progress progress = info.getProgress();
    var console = provUtil.getConsole();
    console.println(String.format("status: %s%n", info.getStatus()));
    if (progress != null) {
      console.println(String.format(
              "progress: numSucceeded=%d, numFailed=%d, numRemaining=%d%n",
              progress.getNumSucceeded(), progress.getNumFailed(), progress.getNumRemaining()));
    }
  }
}

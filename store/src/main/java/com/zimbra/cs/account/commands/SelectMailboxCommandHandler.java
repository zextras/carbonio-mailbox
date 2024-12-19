package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.zclient.ZClientException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.zclient.ZMailboxUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class SelectMailboxCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public SelectMailboxCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, IOException {
    var prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    ZMailboxUtil util = new ZMailboxUtil();
    util.setVerbose(provUtil.getVerboseMode());
    util.setDebug(provUtil.getDebugLevel() != ProvUtil.SoapDebugLevel.none);
    boolean smInteractive = provUtil.getInteractiveMode() && args.length < 3;
    util.setInteractive(smInteractive);
    util.selectMailbox(args[1], (SoapProvisioning) prov);
    if (smInteractive) {
      util.interactive(provUtil.getCliReader());
    } else if (args.length > 2) {
      String[] newArgs = new String[args.length - 2];
      System.arraycopy(args, 2, newArgs, 0, newArgs.length);
      util.execute(newArgs);
    } else {
      throw ZClientException.CLIENT_ERROR(
              "command only valid in interactive mode or with arguments", null);
    }
  }
}

package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import org.apache.http.HttpException;

import java.io.IOException;

class VerifyIndexCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public VerifyIndexCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doVerifyIndex(args);
  }

  private void doVerifyIndex(String[] args) throws ServiceException {
    var prov = provUtil.getProvisioning();
    if (!(prov instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }
    var console = provUtil.getConsole();
    console.println("Verifying, on a large index it can take quite a long time...");
    SoapProvisioning soap = (SoapProvisioning) prov;
    SoapProvisioning.VerifyIndexResult result = soap.verifyIndex(provUtil.lookupAccount(args[1]));
    console.println();
    console.print(result.message);
    if (!result.status) {
      throw ServiceException.FAILURE(
              "The index may be corrupted. Run reIndexMailbox(rim) to repair.", null);
    }
  }
}

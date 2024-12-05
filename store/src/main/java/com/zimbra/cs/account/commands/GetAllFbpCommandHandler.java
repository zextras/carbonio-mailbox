package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.fb.FbCli;
import org.apache.http.HttpException;

import java.io.IOException;

public class GetAllFbpCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllFbpCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetAllFreeBusyProviders();
  }

  private void doGetAllFreeBusyProviders() throws ServiceException, IOException, HttpException {
    FbCli fbcli = new FbCli();
    for (FbCli.FbProvider fbprov : fbcli.getAllFreeBusyProviders()) {
      provUtil.getConsole().println(fbprov.toString());
    }
  }
}

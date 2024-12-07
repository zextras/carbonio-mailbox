package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.fb.FbCli;
import org.apache.http.HttpException;

import java.io.IOException;

public class GetFreebusyQueueInfoCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetFreebusyQueueInfoCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetFreeBusyQueueInfo(args);
  }

  private void doGetFreeBusyQueueInfo(String[] args)
          throws ServiceException, IOException, HttpException {
    FbCli fbcli = new FbCli();
    String name = null;
    if (args.length > 1) {
      name = args[1];
    }
    for (FbCli.FbQueue fbqueue : fbcli.getFreeBusyQueueInfo(name)) {
      provUtil.getConsole().println(fbqueue.toString());
    }
  }
}

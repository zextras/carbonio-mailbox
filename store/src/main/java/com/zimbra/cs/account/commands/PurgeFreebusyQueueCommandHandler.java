package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.fb.FbCli;
import org.apache.http.HttpException;

import java.io.IOException;

class PurgeFreebusyQueueCommandHandler implements CommandHandler {

  @Override public void handle(String[] args) throws ServiceException, HttpException, IOException {
    doPurgeFreeBusyQueue(args);
  }

  private void doPurgeFreeBusyQueue(String[] args)
          throws ServiceException, IOException, HttpException {
    String provider = null;
    if (args.length > 1) {
      provider = args[1];
    }
    FbCli fbcli = new FbCli();
    fbcli.purgeFreeBusyQueue(provider);
  }
}

package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import org.apache.http.HttpException;

import java.io.IOException;

public class SetPasswordCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public SetPasswordCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    Provisioning.SetPasswordResult result = provUtil.getProvisioning().setPassword(provUtil.lookupAccount(args[1]), args[2]);
    if (result.hasMessage()) {
      provUtil.getConsole().println(result.getMessage());
    }
  }
}

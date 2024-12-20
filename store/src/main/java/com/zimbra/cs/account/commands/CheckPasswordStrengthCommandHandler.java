package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class CheckPasswordStrengthCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CheckPasswordStrengthCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    provUtil.getProvisioning().checkPasswordStrength(provUtil.lookupAccount(args[1]), args[2]);
    provUtil.getConsole().println("Password passed strength check.");
  }
}

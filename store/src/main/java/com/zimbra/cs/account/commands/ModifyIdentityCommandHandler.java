package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import java.io.IOException;
import org.apache.http.HttpException;

class ModifyIdentityCommandHandler implements CommandHandler {

  private final ProvUtil provUtil;

  public ModifyIdentityCommandHandler(final ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override
  public void handle(final String[] args)
      throws ServiceException, ArgException {
    var account = provUtil.lookupAccount(args[1]);
    provUtil.getProvisioning().modifyIdentity(account, args[2], provUtil.getMapAndCheck(args, 3, false));
  }
}

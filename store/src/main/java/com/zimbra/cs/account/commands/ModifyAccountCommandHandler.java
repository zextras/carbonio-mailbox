package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.common.cli.ExitCodeException;
import java.io.IOException;
import org.apache.http.HttpException;

class ModifyAccountCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public ModifyAccountCommandHandler(final ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override
  public void handle(final String[] args)
			throws ServiceException, ArgException, ExitCodeException {
    provUtil.getProvisioning().modifyAttrs(provUtil.lookupAccount(args[1]), provUtil.getMapAndCheck(args, 2, false), true);
  }
}

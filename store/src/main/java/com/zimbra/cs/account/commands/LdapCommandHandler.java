package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import org.apache.http.HttpException;

import java.io.IOException;

public class LdapCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public LdapCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    // HACK FOR NOW
    provUtil.setProvisioning( Provisioning.getInstance() );
  }
}

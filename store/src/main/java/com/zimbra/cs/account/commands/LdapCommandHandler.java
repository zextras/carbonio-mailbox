package com.zimbra.cs.account.commands;

import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;

class LdapCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public LdapCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) {
    // HACK FOR NOW
    provUtil.setProvisioning( Provisioning.getInstance() );
  }
}

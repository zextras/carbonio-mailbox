package com.zimbra.cs.account.commands;

import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;

class ExitCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public ExitCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) {
    System.exit(provUtil.getErrorOccursDuringInteraction() ? 2 : 0);
  }
}

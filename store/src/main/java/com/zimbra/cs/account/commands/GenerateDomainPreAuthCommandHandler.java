package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.PreAuthKey;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;

import java.util.HashMap;

class GenerateDomainPreAuthCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GenerateDomainPreAuthCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doGenerateDomainPreAuth(args);
  }

  private void doGenerateDomainPreAuth(String[] args) throws ServiceException {
    String key = args[1];
    Domain domain = provUtil.lookupDomain(key);
    String preAuthKey = domain.getAttr(Provisioning.A_zimbraPreAuthKey, null);
    if (preAuthKey == null) {
      throw ServiceException.INVALID_REQUEST("domain not configured for preauth", null);
    }
    String name = args[2];
    String by = args[3];
    long timestamp = Long.parseLong(args[4]);
    if (timestamp == 0) {
      timestamp = System.currentTimeMillis();
    }
    long expires = Long.parseLong(args[5]);
    HashMap<String, String> params = new HashMap<>();
    params.put("account", name);
    params.put("by", by);
    params.put("timestamp", timestamp + "");
    params.put("expires", expires + "");
    if (args.length == 7) {
      params.put("admin", args[6]);
    }
    provUtil.getConsole().print(String.format(
            "account: %s\nby: %s\ntimestamp: %s\nexpires: %s\npreauth: %s\n",
            name, by, timestamp, expires, PreAuthKey.computePreAuth(params, preAuthKey)));
  }
}

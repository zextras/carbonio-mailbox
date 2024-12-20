package com.zimbra.cs.account.commands;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.PreAuthKey;
import com.zimbra.cs.account.ProvUtil;

import java.util.HashMap;

class GenerateDomainPreAuthKeyCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GenerateDomainPreAuthKeyCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGenerateDomainPreAuthKey(args);
  }

  private void doGenerateDomainPreAuthKey(String[] args) throws ServiceException {
    String key = null;
    boolean force = false;
    if (args.length == 3) {
      if (args[1].equals("-f")) {
        force = true;
      } else {
        provUtil.usage();
        return;
      }
      key = args[2];
    } else {
      key = args[1];
    }

    Domain domain = provUtil.lookupDomain(key);
    String curPreAuthKey = domain.getAttr(ZAttrProvisioning.A_zimbraPreAuthKey);
    if (curPreAuthKey != null && !force) {
      throw ServiceException.INVALID_REQUEST(
              "pre auth key exists for domain "
                      + key
                      + ", use command -f option to force overwriting the existing key",
              null);
    }
    String preAuthKey = PreAuthKey.generateRandomPreAuthKey();
    HashMap<String, String> attrs = new HashMap<>();
    attrs.put(ZAttrProvisioning.A_zimbraPreAuthKey, preAuthKey);
    provUtil.getProvisioning().modifyAttrs(domain, attrs);
    var console = provUtil.getConsole();
    console.print(String.format("preAuthKey: %s%n", preAuthKey));
    if (curPreAuthKey != null) {
      console.print(String.format("previous preAuthKey: %s%n", curPreAuthKey));
    }
  }
}

package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.UsageException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.soap.admin.type.CountObjectsType;

class CountObjectsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CountObjectsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, UsageException {
    doCountObjects(args);
  }

  private void doCountObjects(String[] args) throws ServiceException, UsageException {

    CountObjectsType type = CountObjectsType.fromString(args[1]);

    Domain domain = null;
    int idx = 2;
    while (args.length > idx) {
      String arg = args[idx];

      if (arg.equals("-d")) {
        if (domain != null) {
          throw ServiceException.INVALID_REQUEST(
                  "domain is already specified as:" + domain.getName(), null);
        }
        idx++;
        if (args.length <= idx) {
          provUtil.usage();
          throw ServiceException.INVALID_REQUEST("expecting domain, not enough args", null);
        }
        domain = provUtil.lookupDomain(args[idx]);
      } else {
        provUtil.usage();
        return;
      }

      idx++;
    }
    long result = provUtil.getProvisioning().countObjects(type, domain);
    provUtil.getConsole().println(result);
  }
}

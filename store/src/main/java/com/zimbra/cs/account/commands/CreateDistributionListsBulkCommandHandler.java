package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class CreateDistributionListsBulkCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CreateDistributionListsBulkCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doCreateDistributionListsBulk(args);
  }

  private void doCreateDistributionListsBulk(String[] args) throws ServiceException {
    if (args.length < 3) {
      provUtil.usage();
    } else {
      String domain = args[1];
      String nameMask = args[2];
      int numAccounts = Integer.parseInt(args[3]);
      for (int i = 0; i < numAccounts; i++) {
        String name = nameMask + i + "@" + domain;
        Map<String, Object> attrs = new HashMap<>();
        String displayName = nameMask + " N. " + i;
        StringUtil.addToMultiMap(attrs, "displayName", displayName);
        DistributionList dl = provUtil.getProvisioning().createDistributionList(name, attrs);
        provUtil.getConsole().println(dl.getId());
      }
    }
  }
}

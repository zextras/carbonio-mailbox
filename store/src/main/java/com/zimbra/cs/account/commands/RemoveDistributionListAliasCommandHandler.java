package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.ProvUtil;

class RemoveDistributionListAliasCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public RemoveDistributionListAliasCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    Group dl = provUtil.lookupGroup(args[1], false);
    // Even if dl is null, we still invoke removeAlias.
    // This is so dangling aliases can be cleaned up as much as possible.
    // If dl is null, the NO_SUCH_DISTRIBUTION_LIST thrown by SOAP will contain
    // null as the dl identity, because SoapProvisioning sends no id to the server.
    // In this case, we catch the NO_SUCH_DISTRIBUTION_LIST and throw another one
    // with the named/id entered on the comand line.
    try {
      provUtil.getProvisioning().removeGroupAlias(dl, args[2]);
    } catch (ServiceException e) {
      if (!(dl == null
              && AccountServiceException.NO_SUCH_DISTRIBUTION_LIST.equals(e.getCode()))) {
        throw e;
      }
      // else eat the exception, we will throw below
    }
    if (dl == null) {
      throw AccountServiceException.NO_SUCH_DISTRIBUTION_LIST(args[1]);
    }
  }
}

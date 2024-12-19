package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.ldap.LdapProv;
import org.apache.http.HttpException;

import java.io.IOException;

class RenameAccountCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public RenameAccountCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doRenameAccount(args);
  }

  private void doRenameAccount(String[] args) throws ServiceException {
    var prov = provUtil.getProvisioning();
    if (prov instanceof LdapProv) {
      boolean confirmed =
              provUtil.confirm(
                      "-l option is specified.  "
                              + "Only the LDAP portion of the account will be deleted.\n"
                              + "DB data of the account will not be renamed.\n");

      if (!confirmed) {
        provUtil.getConsole().println("aborted");
        return;
      }
    }

    prov.renameAccount(provUtil.lookupAccount(args[1]).getId(), args[2]);
  }

}

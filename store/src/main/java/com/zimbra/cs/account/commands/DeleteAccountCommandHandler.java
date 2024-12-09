package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.ldap.LdapProv;
import org.apache.http.HttpException;

import java.io.IOException;

public class DeleteAccountCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public DeleteAccountCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doDeleteAccount(args);
  }

  private void doDeleteAccount(String[] args) throws ServiceException {
    var prov = provUtil.getProvisioning();
    var console = provUtil.getConsole();
    if (prov instanceof LdapProv) {
      boolean confirmed =
              provUtil.confirm(
                      "-l option is specified.  Only the LDAP entry of the account will be"
                              + " deleted.\n"
                              + "DB data of the account and associated blobs will not be"
                              + " deleted.\n");

      if (!confirmed) {
        console.println("aborted");
        return;
      }
    }

    String key = args[1];
    Account acct = provUtil.lookupAccount(key);
    if (key.equalsIgnoreCase(acct.getId())
            || key.equalsIgnoreCase(acct.getName())
            || acct.getName().equalsIgnoreCase(key + "@" + acct.getDomainName())) {
      prov.deleteAccount(acct.getId());
    } else {
      throw ServiceException.INVALID_REQUEST(
              "argument to deleteAccount must be an account id or the account's primary name", null);
    }
  }
}

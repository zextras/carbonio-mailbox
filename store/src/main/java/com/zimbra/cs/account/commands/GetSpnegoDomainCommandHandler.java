package com.zimbra.cs.account.commands;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class GetSpnegoDomainCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetSpnegoDomainCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetSpnegoDomain();
  }

  private void doGetSpnegoDomain() throws ServiceException {
    var prov = provUtil.getProvisioning();
    Config config = prov.getConfig();
    String spnegoAuthRealm = config.getSpnegoAuthRealm();
    if (spnegoAuthRealm != null) {
      Domain domain = prov.get(Key.DomainBy.krb5Realm, spnegoAuthRealm);
      if (domain != null) {
        provUtil.getConsole().println(domain.getName());
      }
    }
  }
}

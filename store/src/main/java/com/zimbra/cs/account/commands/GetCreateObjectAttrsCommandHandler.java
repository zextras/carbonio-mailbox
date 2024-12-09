package com.zimbra.cs.account.commands;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.soap.admin.type.GranteeSelector;
import org.apache.http.HttpException;

import java.io.IOException;

class GetCreateObjectAttrsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetCreateObjectAttrsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetCreateObjectAttrs(args);
  }

  private void doGetCreateObjectAttrs(String[] args) throws ServiceException {
    String targetType = args[1];

    Key.DomainBy domainBy = null;
    String domain = null;
    if (!args[2].equals("null")) {
      domainBy = ProvUtil.guessDomainBy(args[2]);
      domain = args[2];
    }

    Key.CosBy cosBy = null;
    String cos = null;
    if (!args[3].equals("null")) {
      cosBy = ProvUtil.guessCosBy(args[3]);
      cos = args[3];
    }

    GranteeSelector.GranteeBy granteeBy = null;
    String grantee = null;

    // take grantee arg only if LdapProv
    // for SoapProvisioning, -a {admin account} -p {password} is required with zmprov
    var prov = provUtil.getProvisioning();
    if (prov instanceof LdapProv) {
      granteeBy = ProvUtil.guessGranteeBy(args[4]);
      grantee = args[4];
    }

    var console = provUtil.getConsole();
    console.println("Domain:  " + domain);
    console.println("Cos:     " + cos);
    console.println("Grantee: " + grantee);
    console.println();

    RightCommand.EffectiveRights effRights =
            prov.getCreateObjectAttrs(targetType, domainBy, domain, cosBy, cos, granteeBy, grantee);
    provUtil.displayAttrs("set", true, effRights.canSetAllAttrs(), effRights.canSetAttrs());
  }

}

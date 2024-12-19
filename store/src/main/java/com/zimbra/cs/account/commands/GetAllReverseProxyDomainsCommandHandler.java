package com.zimbra.cs.account.commands;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class GetAllReverseProxyDomainsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllReverseProxyDomainsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException {
    doGetAllReverseProxyDomains();
  }

  private void doGetAllReverseProxyDomains() throws ServiceException {
    NamedEntry.Visitor visitor =
            entry -> {
              if (entry.getAttr(ZAttrProvisioning.A_zimbraVirtualHostname) != null
                      && entry.getAttr(ZAttrProvisioning.A_zimbraSSLPrivateKey) != null
                      && entry.getAttr(ZAttrProvisioning.A_zimbraSSLCertificate) != null) {
                StringBuilder virtualHosts = new StringBuilder();
                for (String vh : entry.getMultiAttr(ZAttrProvisioning.A_zimbraVirtualHostname)) {
                  virtualHosts.append(vh).append(" ");
                }
                provUtil.getConsole().println(entry.getName() + " " + virtualHosts);
              }
            };

    provUtil.getProvisioning().getAllDomains(
            visitor,
            new String[] {
                    ZAttrProvisioning.A_zimbraVirtualHostname,
                    ZAttrProvisioning.A_zimbraSSLPrivateKey,
                    ZAttrProvisioning.A_zimbraSSLCertificate
            });
  }
}

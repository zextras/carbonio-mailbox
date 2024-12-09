package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import org.apache.http.HttpException;

import java.io.IOException;

public class GetAllReverseProxyDomainsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllReverseProxyDomainsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetAllReverseProxyDomains();
  }

  private void doGetAllReverseProxyDomains() throws ServiceException {
    NamedEntry.Visitor visitor =
            entry -> {
              if (entry.getAttr(Provisioning.A_zimbraVirtualHostname) != null
                      && entry.getAttr(Provisioning.A_zimbraSSLPrivateKey) != null
                      && entry.getAttr(Provisioning.A_zimbraSSLCertificate) != null) {
                StringBuilder virtualHosts = new StringBuilder();
                for (String vh : entry.getMultiAttr(Provisioning.A_zimbraVirtualHostname)) {
                  virtualHosts.append(vh).append(" ");
                }
                provUtil.getConsole().println(entry.getName() + " " + virtualHosts);
              }
            };

    provUtil.getProvisioning().getAllDomains(
            visitor,
            new String[] {
                    Provisioning.A_zimbraVirtualHostname,
                    Provisioning.A_zimbraSSLPrivateKey,
                    Provisioning.A_zimbraSSLCertificate
            });
  }
}

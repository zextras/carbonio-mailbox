package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.generated.AdminRights;
import com.zimbra.cs.rmgmt.RemoteCertbot;
import com.zimbra.cs.rmgmt.RemoteManager;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;
import java.util.Optional;

public class IssueCert extends AdminDocumentHandler {

  @Override
  public Element handle(final Element request, final Map<String, Object> context)
      throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    Provisioning prov = Provisioning.getInstance();
    String domainId = request.getAttribute(AdminConstants.A_DOMAIN);
    Domain domain = prov.get(DomainBy.id, domainId);

    if (domain == null) {
      throw ServiceException.INVALID_REQUEST(
          "Domain with id " + domainId + " could not be found.", null);
    }

    AdminAccessControl admin = checkDomainRight(zsc, domain, AdminRights.R_getDomain);
    String adminMail = admin.mAuthedAcct.getMail();

    String publicServiceHostname = domain.getPublicServiceHostname();
    String[] virtualHostNames = domain.getVirtualHostname();
    if (publicServiceHostname == null || virtualHostNames.length == 0) {
      throw ServiceException.INVALID_REQUEST(
          "Domain with id " + domainId + " must have PublicServiceHostname and VirtualHostName.", null);
    }

//    String serverName = "nbm-m01.demo.zextras.io";
//    Server server = prov.get(Key.ServerBy.name, serverName);

    Optional<Server> optionalServer = prov.getAllServers()
        .stream()
        .filter(Server::hasProxyService)
        .findFirst();

    if (optionalServer.isEmpty()) {
      throw ServiceException.NOT_FOUND("Server with carbonio-proxy node could not be found.");
    }

    ZimbraLog.security.info("Issuing a LetsEncrypt cert for domain: " + domainId);

    RemoteManager remoteManager = RemoteManager.getRemoteManager(optionalServer.get());
    RemoteCertbot certbot = new RemoteCertbot(remoteManager);

    String result = certbot.dryRun(adminMail, publicServiceHostname, virtualHostNames);

//    String exc = null;
//    String output = null;
//    try {
//      RemoteResult remoteResult = remoteManager.execute(RemoteCommands.ZM_SERVER_IPS);
//      byte[] stdOut = remoteResult.getMStdout();
//      output = new String(stdOut);
//    } catch (ServiceException e) {
//      exc = e.getCode() + " " + e.getMessage();
//    }


    ZimbraLog.security.info("Issuing cert result: " + result);

    Element response = zsc.createElement(AdminConstants.ISSUE_CERT_RESPONSE);

    Element responseMessageElement =
        response
            .addNonUniqueElement(AdminConstants.E_MESSAGE)
            .addAttribute(AdminConstants.A_DOMAIN, domain.getName());
    //responseMessageElement.setText(" !" + exc + "! " + output);
    responseMessageElement.setText(result);


    return response;
  }

}

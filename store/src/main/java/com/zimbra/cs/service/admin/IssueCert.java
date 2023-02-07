package com.zimbra.cs.service.admin;

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
import com.zimbra.cs.rmgmt.RemoteCommands;
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

    String chain = request.getAttribute(AdminConstants.A_CHAIN);

    String publicServiceHostname = domain.getPublicServiceHostname();
    String[] virtualHostNames = domain.getVirtualHostname();
    if (publicServiceHostname == null || virtualHostNames.length == 0) {
      throw ServiceException.INVALID_REQUEST(
          "Domain with id " + domainId + " must have PublicServiceHostname and VirtualHostName.",
          null);
    }

    Optional<Server> optionalServer =
        prov.getAllServers().stream().filter(Server::hasProxyService).findFirst();

    if (optionalServer.isEmpty()) {
      throw ServiceException.NOT_FOUND("Server with carbonio-proxy node could not be found.");
    }

    ZimbraLog.rmgmt.info("Issuing a LetsEncrypt cert for domain " + domainId);

    RemoteManager remoteManager = RemoteManager.getRemoteManager(optionalServer.get());
    RemoteCertbot certbot = new RemoteCertbot(remoteManager);
    String command = certbot.createCommand(
        RemoteCommands.CERTBOT_DRY_RUN, adminMail, chain, publicServiceHostname, virtualHostNames);
    String result = certbot.execute(command);

    ZimbraLog.rmgmt.info("Issuing a LetsEncrypt cert for domain " + domainId + " result: " + result);

    Element response = zsc.createElement(AdminConstants.ISSUE_CERT_RESPONSE);

    Element responseMessageElement =
        response
            .addNonUniqueElement(AdminConstants.E_MESSAGE)
            .addAttribute(AdminConstants.A_DOMAIN, domain.getName());
    responseMessageElement.setText(result);

    return response;
  }
}

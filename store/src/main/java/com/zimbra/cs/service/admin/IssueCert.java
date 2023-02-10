package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.generated.AdminRights;
import com.zimbra.cs.rmgmt.RemoteCertbotCmd;
import com.zimbra.cs.rmgmt.RemoteCommands;
import com.zimbra.cs.rmgmt.RemoteManager;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;
import java.util.Optional;

/**
 * Admin Handler class to issue a LetsEncrypt certificate for a domain using
 * {@link com.zimbra.cs.rmgmt.RemoteManager}, {@link RemoteCertbotCmd}.
 *
 * @author Yuliya Aheeva
 * @since 23.3.0
 */
public class IssueCert extends AdminDocumentHandler {

  /**
   * Handles the request. Searches a domain by id, checks admin rights (accessible to global and
   * delegated admin of requested domain), searches a server with proxy node, executes remote
   * certbot command on it, creates response element.
   *
   * @param request {@link Element} representation of {@link
   *     com.zimbra.soap.admin.message.IssueCertRequest}
   * @param context request context
   * @return {@link Element} representation of {@link
   *     com.zimbra.soap.admin.message.IssueCertResponse}
   * @throws ServiceException in case if domain could not be found, domain doesn't have
   * PublicServiceHostname and at least one VirtualHostName,
   * server with proxy node could not be found or domain admin dosn't have rights to deal with
   * this domain.
   *
   * It won't throw an exception in case if remote execution command fails, instead will create
   * a response and add a failure message to it.
   */
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

    String chain = request.getAttribute(AdminConstants.A_CHAIN, AdminConstants.DEFAULT_CHAIN);
    boolean expand = request.getAttributeBool(MailConstants.A_EXPAND, false);

    String publicServiceHostname = domain.getPublicServiceHostname();
    String[] virtualHostNames = domain.getVirtualHostname();
    if (publicServiceHostname == null || virtualHostNames.length == 0) {
      throw ServiceException.INVALID_REQUEST(
          "Domain with id " + domainId + " must have PublicServiceHostname and VirtualHostName.",
          null);
    }

    // First release will work only on ONE proxy even if there are multiple proxy in the infrastructure.
    Optional<Server> optionalServer =
        prov.getAllServers()
            .stream()
            .filter(Server::hasProxyService)
            .findFirst();

    if (optionalServer.isEmpty()) {
      throw ServiceException.NOT_FOUND("Server with carbonio-proxy node could not be found.");
    }

    ZimbraLog.rmgmt.info("Issuing a LetsEncrypt cert for domain " + domainId);

    RemoteManager remoteManager = RemoteManager.getRemoteManager(optionalServer.get());
    RemoteCertbotCmd certbot = new RemoteCertbotCmd(remoteManager);
    String command =
        certbot.createCommand(
            RemoteCommands.CERTBOT_CERTONLY,
            adminMail,
            chain,
            expand,
            publicServiceHostname,
            virtualHostNames);
    String result = certbot.execute(command);

    ZimbraLog.rmgmt.info(
        "Issuing a LetsEncrypt cert for domain " + domainId + " result: " + result);

    Element response = zsc.createElement(AdminConstants.ISSUE_CERT_RESPONSE);

    Element responseMessageElement =
        response
            .addNonUniqueElement(AdminConstants.E_MESSAGE)
            .addAttribute(AdminConstants.A_DOMAIN, domain.getName());
    responseMessageElement.setText(result);

    return response;
  }
}

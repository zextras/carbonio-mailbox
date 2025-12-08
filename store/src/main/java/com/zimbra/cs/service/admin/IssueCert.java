package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.generated.AdminRights;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.rmgmt.RemoteCertbot;
import com.zimbra.cs.rmgmt.RemoteCommands;
import com.zimbra.cs.rmgmt.RemoteManager;
import com.zimbra.soap.ZimbraSoapContext;
import io.vavr.Function2;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Admin Handler class to issue a LetsEncrypt certificate for a domain using {@link com.zimbra.cs.rmgmt.RemoteManager},
 * {@link RemoteCertbot}.
 *
 * @author Yuliya Aheeva
 * @since 23.3.0
 */
public class IssueCert extends AdminDocumentHandler {

  private final Function<RemoteManager,RemoteCertbot> certBotProvider;
  private final Function2<Mailbox, Domain, CertificateNotificationManager> notificationManagerProvider;
  public static final String RESPONSE =
      "The System is processing your certificate generation request.\n"
          + "It will send the result to the Global and Domain notification recipients.";

	public IssueCert(Function<RemoteManager, RemoteCertbot> certBotProvider,
			Function2<Mailbox, Domain, CertificateNotificationManager> notificationManagerProvider) {
		this.certBotProvider = certBotProvider;
		this.notificationManagerProvider = notificationManagerProvider;
	}

	/**
   * Handles the request. Searches a domain by id, checks admin rights (accessible to global and delegated admin of
   * requested domain), searches a server with proxy node, creates certbot command, asynchronously executes it, creates
   * response element, notifies global and domain recipients about the result of remote execution.
   *
   * <p>Note: Executes certbot command only on ONE (first found) proxy even if there are multiple
   * proxy in the infrastructure.
   *
   * @param request {@link Element} representation of {@link com.zimbra.soap.admin.message.IssueCertRequest}
   * @param context request context
   * @return {@link Element} representation of {@link com.zimbra.soap.admin.message.IssueCertResponse}
   * @throws ServiceException in case if domain could not be found, domain doesn't have PublicServiceHostname and at
   *                          least one VirtualHostName, server with proxy node could not be found or domain admin
   *                          doesn't have rights to deal with this domain.
   */
  @Override
  public Element handle(final Element request, final Map<String, Object> context)
      throws ServiceException {

    final ZimbraSoapContext zsc = getZimbraSoapContext(context);
    final Provisioning prov = Provisioning.getInstance();
    final Account callerAccount = zsc.getAuthToken().getAccount();

    if (!prov.onLocalServer(callerAccount)) {
      return proxyRequestToAccountServer(request, context, callerAccount);
    }

    final String domainId = request.getAttribute(AdminConstants.A_DOMAIN);
    final Domain domain =
        Optional.ofNullable(prov.get(DomainBy.id, domainId))
            .orElseThrow(
                () ->
                    ServiceException.INVALID_REQUEST(
                        "Domain with id " + domainId + " could not be found.", null));

    final AdminAccessControl adminAccessControl =
        checkDomainRight(zsc, domain, AdminRights.R_setDomainAdminDomainAttrs);
    final String adminMail = adminAccessControl.mAuthedAcct.getMail();

    final String domainName =
        Optional.ofNullable(domain.getDomainName())
            .orElseThrow(
                () ->
                    ServiceException.FAILURE(
                        "Domain with id " + domainId + " must have domain name", null));
    final String publicServiceHostname =
        Optional.ofNullable(domain.getPublicServiceHostname())
            .orElseThrow(
                () ->
                    ServiceException.FAILURE(
                        "Domain " + domainName + " must have PublicServiceHostname.", null));
    final String[] virtualHostNames =
        Optional.ofNullable(domain.getVirtualHostname())
            .filter(hosts -> hosts.length > 0)
            .orElseThrow(
                () ->
                    ServiceException.FAILURE(
                        "Domain " + domainName + " must have at least one VirtualHostName."));

    final Server proxyServer =
        prov.getAllServers().stream()
            .filter(Server::hasProxyService)
            .findFirst()
            .orElseThrow(
                () ->
                    ServiceException.FAILURE(
                        "Issuing LetsEncrypt certificate command requires carbonio-proxy. "
                            + "Make sure carbonio-proxy is installed, up and running."));

    ZimbraLog.rmgmt.info("Issuing LetsEncrypt cert for domain " + domainName);

    final RemoteManager remoteManager = RemoteManager.getRemoteManager(proxyServer);
    final RemoteCertbot certbot = certBotProvider.apply(remoteManager);
    final String command =
        certbot.createCommand(
            RemoteCommands.CERTBOT_CERTONLY,
            adminMail,
            domainName,
            publicServiceHostname,
            virtualHostNames);

    final Mailbox mbox = getRequestedMailbox(zsc);
    final CertificateNotificationManager certificateNotificationManager =
        notificationManagerProvider.apply(mbox, domain);

    certbot.supplyAsync(certificateNotificationManager, command);

    final Element response = zsc.createElement(AdminConstants.ISSUE_CERT_RESPONSE);

    final Element responseMessageElement =
        response
            .addNonUniqueElement(AdminConstants.E_MESSAGE)
            .addAttribute(AdminConstants.A_DOMAIN, domainName);

    responseMessageElement.setText(RESPONSE);

    return response;
  }

  Element proxyRequestToAccountServer(Element request, Map<String, Object> context, Account account)
      throws ServiceException {
    ZimbraLog.soap.info("Proxying IssueCert request to " + account.getName() + "'s mail host.");
    return proxyRequest(request, context, account.getId());
  }
}

package com.zimbra.cert;

import com.zimbra.cert.util.X509CertificateParser;
import com.zimbra.common.account.Key.DomainBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.CertMgrConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.generated.AdminRights;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Admin Handler class to get information about a domain certificate.
 *
 * @author Yuliya Aheeva
 * @since 23.2.0
 */
public class GetDomainCert extends AdminDocumentHandler {
  private static final String DATE_PATTERN = "MMM dd yyyy HH:mm:ss z";

  /**
   * Handles the request. Searches a domain by id, checks admin rights (accessible to global and
   * delegated admin of requested domain), decrypts X.509 certificate, creates response element.
   *
   * @param request {@link Element} representation of {@link
   *     com.zimbra.soap.admin.message.GetDomainCertRequest}
   * @param context request context.
   * @return {@link Element} representation of {@link
   *     com.zimbra.soap.admin.message.GetDomainCertResponse}
   * @throws ServiceException in case if a requested domain could not be found or if an error occurs
   *     during certificate parsing.
   */
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    Provisioning prov = Provisioning.getInstance();
    String domainId = request.getAttribute(AdminConstants.A_DOMAIN);
    Domain domain = prov.get(DomainBy.id, domainId);

    if (domain == null) {
      throw ServiceException.INVALID_REQUEST(
          "Domain with id " + domainId + " could not be found.", null);
    }

    if (domain.getSSLCertificate() == null) {
      throw ServiceException.INVALID_REQUEST(
          "Certificate for Domain with id " + domainId + " could not be found.", null);
    }

    checkDomainRight(zsc, domain, AdminRights.R_getDomain);

    X509Certificate x509Certificate =
        X509CertificateParser.generateCertificate(domain.getSSLCertificate().getBytes());

    ZimbraLog.security.info("Parsing the cert info for domain: " + domainId);

    Element response = zsc.createElement(CertMgrConstants.GET_DOMAIN_CERT_RESPONSE);

    Element responseCertElement =
        response
            .addNonUniqueElement(CertMgrConstants.E_cert)
            .addAttribute(AdminConstants.A_DOMAIN, domain.getDomainName());
    fillResponseCertElem(responseCertElement, x509Certificate);

    return response;
  }

  private void fillResponseCertElem(Element el, X509Certificate cert) throws ServiceException {

    Collection<List<?>> subjectAltNamesCollection = X509CertificateParser.getSubjectAltNames(cert);
    String subjectAltNames = X509CertificateParser.parseSubjectAltNames(subjectAltNamesCollection);

    addChildElem(el, CertMgrConstants.E_SUBJECT, cert.getSubjectX500Principal().getName());
    addChildElem(el, CertMgrConstants.E_SUBJECT_ALT_NAME, subjectAltNames);
    addChildElem(el, CertMgrConstants.E_ISSUER, cert.getIssuerX500Principal().getName());
    addChildElem(el, CertMgrConstants.E_NOT_BEFORE, formatDate(cert.getNotBefore()));
    addChildElem(el, CertMgrConstants.E_NOT_AFTER, formatDate(cert.getNotAfter()));
  }

  private void addChildElem(Element parentElement, String name, String value) {
    Element childElement = parentElement.addNonUniqueElement(name);
    childElement.setText(value);
  }

  private String formatDate(Date date) {
    DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
    return dateFormat.format(date);
  }
}

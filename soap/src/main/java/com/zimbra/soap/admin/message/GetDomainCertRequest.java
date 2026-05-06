package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.CertMgrConstants;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Domain Certificate <br>
 *     Gets the certificate of a requested domain.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = CertMgrConstants.E_GET_DOMAIN_CERT_REQUEST)
public class GetDomainCertRequest {

  /**
   * @zm-api-field-tag domain id
   * @zm-api-field-description a domain id whose cert is to be got
   */
  @XmlAttribute(name = AdminConstants.A_DOMAIN /* domain */, required = true)
  private String domain;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private GetDomainCertRequest() {
    this(null);
  }

  public GetDomainCertRequest(String domain) {
    this.domain = domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getDomain() {
    return domain;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("domain", domain);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

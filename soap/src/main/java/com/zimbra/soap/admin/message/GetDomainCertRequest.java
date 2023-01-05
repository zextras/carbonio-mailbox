package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.CertMgrConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Get Domain Certificate
 * <br />
 * Gets the certificate of a requested domain.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name= CertMgrConstants.E_GET_DOMAIN_CERT_REQUEST)
public class GetDomainCertRequest {


  /**
   * @zm-api-field-tag domain-id
   * @zm-api-field-description The domain's ID whose cert is to be got
   */
  @XmlAttribute(name= AdminConstants.A_DOMAIN /* domain */, required=true)
  private final String domain;

  /**
   * no-argument constructor wanted by JAXB
   */
  @SuppressWarnings("unused")
  private GetDomainCertRequest() {
    this(null);
  }

  public GetDomainCertRequest(String domain) {
    this.domain = domain;
  }

  public MoreObjects.ToStringHelper addToStringInfo(
      MoreObjects.ToStringHelper helper) {
    return helper
        .add("domain", domain);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this))
        .toString();
  }

}

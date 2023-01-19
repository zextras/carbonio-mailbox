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
 * @zm-api-command-description Issue a LetsEncrypt Certificate <br>
 *     Issues a LetsEncrypt certificate for a requested domain.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = CertMgrConstants.E_ISSUE_LETSENCRYPT_CERT_REQUEST)
public class IssueLetsEncryptCertRequest {

  /**
   * @zm-api-field-tag domain name
   * @zm-api-field-description a domain name to issue a LetsEncrypt cert for
   */
  @XmlAttribute(name = AdminConstants.A_DOMAIN /* domain */, required = true)
  private String domain;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private IssueLetsEncryptCertRequest() {
    this(null);
  }

  public IssueLetsEncryptCertRequest(String domain) {
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








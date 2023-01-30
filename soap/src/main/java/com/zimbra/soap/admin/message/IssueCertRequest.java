package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.AdminAttrsImpl;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Issue a LetsEncrypt Certificate <br>
 *     Issues a LetsEncrypt certificate for a requested domain.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_ISSUE_CERT_REQUEST)
@XmlType(propOrder = {})
public class IssueCertRequest extends AdminAttrsImpl {

  /**
   * @zm-api-field-tag domain id
   * @zm-api-field-description a domain id to issue a LetsEncrypt certs for
   */
  @XmlAttribute(name = AdminConstants.A_DOMAIN /* domain */, required = true)
  private String domain;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  public IssueCertRequest() {
    this(null);
  }

  public IssueCertRequest(String domain) {
    this.domain = domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getDomain() {
    return domain;
  }
}








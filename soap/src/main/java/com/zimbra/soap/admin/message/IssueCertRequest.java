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
   * @zm-api-field-description a domain id to issue a LetsEncrypt cert for
   */
  @XmlAttribute(name = AdminConstants.A_DOMAIN /* domain */, required = true)
  private String domain;

  /**
   * @zm-api-field-tag preferred certificate chain
   * @zm-api-field-description could be passed zero or one argument - "short". If an argument is
   *     "short" - it will try to issue a short chain cert, in any other cases it will try to issue
   *     a long chain cert.
   *
   *     Long (Default) chain contains:
   *     your leaf certificate
   *     R3 signed by ISRG Root X1 122
   *     ISRG Root X1 signed by DST Root CA X3 93
   *
   *     Short chains contains:
   *     your leaf certificate
   *     R3 signed by ISRG Root X1 122
   */
  @XmlAttribute(name = AdminConstants.A_CHAIN /* chain */, required = false)
  private String chain;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  public IssueCertRequest() {
    this(null, null);
  }

  public IssueCertRequest(String domain, String chain) {
    this.domain = domain;
    this.chain = chain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getDomain() {
    return domain;
  }

  public void setChain(String chain) {
    this.domain = chain;
  }

  public String getChain() {
    return chain;
  }
}

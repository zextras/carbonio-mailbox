package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Issues a LetsEncrypt certificate for a requested domain.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = AdminConstants.E_ISSUE_CERT_REQUEST)
@XmlType(propOrder = {})
public class IssueCertRequest {

  /**
   * @zm-api-field-tag domain id
   * @zm-api-field-description a domain id to issue a LetsEncrypt cert for
   */
  @XmlAttribute(name = AdminConstants.A_DOMAIN /* domain */, required = true)
  private String domain;

  /**
   * @zm-api-field-tag preferred certificate chain
   * @zm-api-field-description could be passed zero or one argument - "short".
   *     By default the chin type is "long". If an admin user pass zero, "long" or random text with
   *     this parameter {@link com.zimbra.cs.service.admin.IssueCert} will handle the request with the
   *     default chain type.
   *     If a user pass "short" with this parameter {@link com.zimbra.cs.service.admin.IssueCert}
   *     will handle the request with the sort chain type.
   *
   *     Long (Default) chain contains:
   *     your leaf certificate
   *     R3 signed by ISRG Root X1 122
   *     ISRG Root X1 signed by DST Root CA X3 93
   *
   *     Short chain contains:
   *     your leaf certificate
   *     R3 signed by ISRG Root X1 122
   */
  @XmlAttribute(name = AdminConstants.A_CHAIN_TYPE /* chainType */, required = false)
  private String chainType;

  /**
   * @zm-api-field-tag expand field requires in order to modify existing domain cert in case if a
   *     virtualHostname was added or deleted for a requested domain.
   * @zm-api-field-description boolean flag.
   * An admin user could pass zero or one argument with this parameter either "true"/"false"
   * or "1"/"0". The default value is false.
   * An admin user should pass "true"/"1" in case if he wants to renew existing certificate
   * the reason is a virtual hostname was added or deleted for a requested domain.
   */
  @XmlAttribute(name = MailConstants.A_EXPAND /* expand */, required = false)
  private ZmBoolean expand;

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

  public void setChain(String chain) {
    this.chainType = chain;
  }

  public String getChain() {
    return chainType;
  }

  public void setExpand(Boolean expand) {
    this.expand = ZmBoolean.fromBool(expand, false);
  }

  public boolean getExpand() {
    return ZmBoolean.toBool(expand, false);
  }
}

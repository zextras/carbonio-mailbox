package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name= AdminConstants.E_ISSUE_CERT_RESPONSE)
public class IssueCertResponse {
  /**
   * @zm-api-field-tag domain-name
   * @zm-api-field-description Domain name
   */
  @XmlAttribute(name = AdminConstants.A_DOMAIN /* domain */, required = true)
  private String domain;

  /**
   * @zm-api-field-tag message
   * @zm-api-field-description message
   */
  @XmlElement(name = AdminConstants.E_MESSAGE /* message */, required = true)
  private String message;

  /**
   * no-argument constructor wanted by JAXB
   */
  public IssueCertResponse() {

  }

  public void setDomain(final String domain) {
    this.domain = domain;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public String getDomain() {
    return domain;
  }

  public String getMessage() {
    return message;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add(AdminConstants.A_DOMAIN, domain)
        .add(AdminConstants.E_MESSAGE, message);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }

}

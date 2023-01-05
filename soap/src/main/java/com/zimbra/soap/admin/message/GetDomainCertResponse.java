package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.CertMgrConstants;
import com.zimbra.soap.admin.type.CertInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=CertMgrConstants.E_GET_DOMAIN_CERT_RESPONSE)
@XmlType(propOrder = {})
public class GetDomainCertResponse {

  /**
   * @zm-api-field-description Certificate information
   */
  @XmlElement(name= CertMgrConstants.E_cert /* cert */, required=false)
  private CertInfo cert;

  public GetDomainCertResponse() {
  }

  public void setCert(CertInfo cert) {
    this.cert = cert;
  }

  public CertInfo getCert() {
    return cert;
  }

  public MoreObjects.ToStringHelper addToStringInfo(
      MoreObjects.ToStringHelper helper) {
    return helper
        .add("cert", cert);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this))
        .toString();
  }

}

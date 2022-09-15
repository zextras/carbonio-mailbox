// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.CertMgrConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Verify Certificate Key
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = CertMgrConstants.E_VERIFY_CERTKEY_REQUEST)
public class VerifyCertKeyRequest {

  /**
   * @zm-api-field-description Certificate (can be chained)
   */
  @XmlAttribute(name = CertMgrConstants.E_cert, required = true)
  private String certificate;

  /**
   * @zm-api-field-description Private key
   */
  @XmlAttribute(name = CertMgrConstants.A_privkey, required = true)
  private String privateKey;

  /**
   * @zm-api-field-description CA (optional if certificate is already chained)
   */
  @XmlAttribute(name = CertMgrConstants.A_ca, required = false)
  private String CA;

  public VerifyCertKeyRequest() {}

  private VerifyCertKeyRequest(String certificate, String privateKey) {
    setCertificate(certificate);
    setPrivateKey(privateKey);
  }

  public static VerifyCertKeyRequest createForCertAndPrivateKey(
      String certificate, String privateKey) {
    return new VerifyCertKeyRequest(certificate, privateKey);
  }

  public void setCertificate(String certificate) {
    this.certificate = certificate;
  }

  public void setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
  }

  public void setCA(String CA) {
    this.CA = CA;
  }

  public String getCertificate() {
    return certificate;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public String getCA() {
    return CA;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("certificate", certificate)
        .add("privateKey", privateKey)
        .add("certificate chain", CA);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

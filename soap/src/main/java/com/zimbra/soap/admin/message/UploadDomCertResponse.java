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

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = CertMgrConstants.E_UPLOAD_DOMCERT_RESPONSE)
public class UploadDomCertResponse {

  /**
   * @zm-api-field-tag certificate-content
   * @zm-api-field-description Certificate content
   */
  @XmlAttribute(name = CertMgrConstants.A_cert_content /* cert_content */, required = false)
  private String certificateContent;

  /**
   * @zm-api-field-tag key-content
   * @zm-api-field-description Key content
   */
  @XmlAttribute(name = CertMgrConstants.A_key_content /* key_content */, required = false)
  private String keyContent;

  private UploadDomCertResponse() {}

  private UploadDomCertResponse(String certificateContent, String keyContent) {
    setCertificateContent(certificateContent);
    setKeyContent(keyContent);
  }

  public static UploadDomCertResponse createForCertificateAndKey(
      String certificateContent, String keyContent) {
    return new UploadDomCertResponse(certificateContent, keyContent);
  }

  public void setCertificateContent(String certificateContent) {
    this.certificateContent = certificateContent;
  }

  public void setKeyContent(String keyContent) {
    this.keyContent = keyContent;
  }

  public String getCertificateContent() {
    return certificateContent;
  }

  public String getKeyContent() {
    return keyContent;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("certificateContent", certificateContent).add("keyContent", keyContent);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

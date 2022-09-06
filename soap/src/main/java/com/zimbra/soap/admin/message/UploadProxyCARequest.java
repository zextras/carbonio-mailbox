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
 * @zm-api-command-description Upload proxy CA
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = CertMgrConstants.E_UPLOAD_PROXYCA_REQUEST)
public class UploadProxyCARequest {

  /**
   * @zm-api-field-description Certificate attach ID
   */
  @XmlAttribute(name = CertMgrConstants.A_CERT_AID /* cert.aid */, required = true)
  private String certificateAttachId;

  /**
   * @zm-api-field-description Certificate name
   */
  @XmlAttribute(name = CertMgrConstants.A_CERT_NAME /* cert.filename */, required = true)
  private String certificateName;

  private UploadProxyCARequest() {}

  private UploadProxyCARequest(String certificateAttachId, String certificateName) {
    setCertificateAttachId(certificateAttachId);
    setCertificateName(certificateName);
  }

  public static UploadProxyCARequest createForAttachIdAndCert(
      String certificateAttachId, String certificateName) {
    return new UploadProxyCARequest(certificateAttachId, certificateName);
  }

  public void setCertificateAttachId(String certificateAttachId) {
    this.certificateAttachId = certificateAttachId;
  }

  public void setCertificateName(String certificateName) {
    this.certificateName = certificateName;
  }

  public String getCertificateAttachId() {
    return certificateAttachId;
  }

  public String getCertificateName() {
    return certificateName;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("certificateAttachId", certificateAttachId)
        .add("certificateName", certificateName);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

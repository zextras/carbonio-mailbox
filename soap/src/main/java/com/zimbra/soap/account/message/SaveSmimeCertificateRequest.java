// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3

package com.zimbra.soap.account.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SmimeConstants;
import com.zimbra.soap.type.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = SmimeConstants.E_SAVE_SMIME_CERTIFICATE_REQUEST)
public class SaveSmimeCertificateRequest {

  /**
   * @zm-api-field-description Upload specification
   */
  @XmlElement(name = MailConstants.E_UPLOAD /* upload */, required = true)
  private Id upload;

  /**
   * @zm-api-field-tag smime-certificate-password
   * @zm-api-field-description password for smime certificate
   */
  @XmlAttribute(name = SmimeConstants.A_CERTIFICATE_PASSWORD /* password */, required = false)
  private String password;

  /**
   * @zm-api-field-tag smime-certificate-replaceId
   * @zm-api-field-description Id of the certificate to be replaced
   */
  @XmlAttribute(name = SmimeConstants.A_REPLACE_ID /* replaceId */, required = false)
  private String replaceId;

  public SaveSmimeCertificateRequest() {
    this(null);
  }

  /**
   * @param upload
   */
  public SaveSmimeCertificateRequest(Id upload) {
    this.upload = upload;
  }

  public void setUpload(Id upload) {
    this.upload = upload;
  }

  public Id getUpload() {
    return upload;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPassword() {
    return password;
  }

  public void setReplaceId(String replaceId) {
    this.replaceId = replaceId;
  }

  public String getReplaceId() {
    return replaceId;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("upload", upload.getId())
        .add("password", password)
        .add("replaceId", replaceId);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

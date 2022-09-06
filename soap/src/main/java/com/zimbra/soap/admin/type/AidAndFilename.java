// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.CertMgrConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class AidAndFilename {

  /**
   * @zm-api-field-tag attachment-id
   * @zm-api-field-description Attachment ID
   */
  @XmlAttribute(name = AdminConstants.A_ATTACHMENT_ID /* aid */, required = false)
  private String attachmentId;

  /**
   * @zm-api-field-tag filename
   * @zm-api-field-description Filename
   */
  @XmlAttribute(name = CertMgrConstants.A_FILENAME /* filename */, required = false)
  private String filename;

  public AidAndFilename() {}

  public AidAndFilename(String attachmentId, String filename) {
    this.setAttachmentId(attachmentId);
    this.setFilename(filename);
  }

  public void setAttachmentId(String attachmentId) {
    this.attachmentId = attachmentId;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getAttachmentId() {
    return attachmentId;
  }

  public String getFilename() {
    return filename;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("attachmentId", attachmentId).add("filename", filename);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

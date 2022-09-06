// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class VoiceMsgUploadInfo {

  /**
   * @zm-api-field-tag upload-id
   * @zm-api-field-description Upload id of the upload. It can be used in subsequent mailing
   *     requests as the attachment id.
   */
  @XmlAttribute(name = MailConstants.A_ID /* id */, required = true)
  private String uploadId;

  public VoiceMsgUploadInfo() {}

  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }

  public String getUploadId() {
    return uploadId;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("uploadId", uploadId);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

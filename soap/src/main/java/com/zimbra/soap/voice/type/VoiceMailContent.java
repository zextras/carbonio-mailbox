// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.VoiceConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class VoiceMailContent {

  /**
   * @zm-api-field-tag url
   * @zm-api-field-description Content servlet relative url for retrieving message content. This url
   *     will retrieve the binary voice content.
   */
  @XmlAttribute(name = MailConstants.A_URL /* url */, required = true)
  private String contentServletRelativeUrl;

  /**
   * @zm-api-field-tag content-type
   * @zm-api-field-description Content type
   */
  @XmlAttribute(name = VoiceConstants.A_CONTENT_TYPE /* ct */, required = true)
  private String contentType;

  public VoiceMailContent() {}

  public void setContentServletRelativeUrl(String contentServletRelativeUrl) {
    this.contentServletRelativeUrl = contentServletRelativeUrl;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getContentServletRelativeUrl() {
    return contentServletRelativeUrl;
  }

  public String getContentType() {
    return contentType;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper
        .add("contentServletRelativeUrl", contentServletRelativeUrl)
        .add("contentType", contentType);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

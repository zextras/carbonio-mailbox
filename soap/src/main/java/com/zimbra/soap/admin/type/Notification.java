// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.NotificationInterface;
import com.zimbra.soap.type.ZmBoolean;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class Notification implements NotificationInterface {

  /**
   * @zm-api-field-tag truncated-flag
   * @zm-api-field-description Truncated flag
   */
  @XmlAttribute(name = MailConstants.A_TRUNCATED_CONTENT /* truncated */, required = false)
  private ZmBoolean truncatedContent;

  /**
   * @zm-api-field-tag content
   * @zm-api-field-description Content
   */
  @XmlElement(name = MailConstants.E_CONTENT /* content */, required = false)
  private String content;

  public Notification() {}

  @Override
  public void setTruncatedContent(Boolean truncatedContent) {
    this.truncatedContent = ZmBoolean.fromBool(truncatedContent);
  }

  @Override
  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public Boolean getTruncatedContent() {
    return ZmBoolean.toBool(truncatedContent);
  }

  @Override
  public String getContent() {
    return content;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("truncatedContent", truncatedContent)
        .add("content", content)
        .toString();
  }
}

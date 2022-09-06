// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Send a delivery report
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_SEND_REPORT_REQUEST)
public class SendDeliveryReportRequest {

  /**
   * @zm-api-field-tag message-id
   * @zm-api-field-description Message ID
   */
  @XmlAttribute(name = MailConstants.A_MESSAGE_ID /* mid */, required = true)
  private final String messageId;

  /** no-argument constructor wanted by JAXB */
  @SuppressWarnings("unused")
  private SendDeliveryReportRequest() {
    this((String) null);
  }

  public SendDeliveryReportRequest(String messageId) {
    this.messageId = messageId;
  }

  public String getMessageId() {
    return messageId;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("messageId", messageId);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

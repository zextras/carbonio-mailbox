// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ChatMessageInfo;
import com.zimbra.soap.mail.type.MessageInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_REMOVE_ATTACHMENTS_RESPONSE)
public class RemoveAttachmentsResponse {

  /**
   * @zm-api-field-description Information about the message
   */
  @XmlElements({
    @XmlElement(name = MailConstants.E_CHAT /* chat */, type = ChatMessageInfo.class),
    @XmlElement(name = MailConstants.E_MSG /* m */, type = MessageInfo.class)
  })
  private MessageInfo message;

  public RemoveAttachmentsResponse() {}

  public void setMessage(MessageInfo message) {
    this.message = message;
  }

  public MessageInfo getMessage() {
    return message;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("message", message);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

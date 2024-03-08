// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.MsgToSend;
import com.zimbra.soap.mail.type.SmartLink;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_SEND_MSG_WITH_SMART_LINKS_REQUEST)
public class SendMsgWithSmartLinksRequest {

  /**
   * @zm-api-field-description Message
   */
  @XmlElement(name = MailConstants.E_MSG, required = true)
  private MsgToSend msg;

  /**
   * @zm-api-field-description Attachments to convert to smart links.
   */
  @XmlElement(name = MailConstants.E_SMART_LINKS, required = false)
  private List<SmartLink> smartLinks;


  public SendMsgWithSmartLinksRequest() {
  }

  public SendMsgWithSmartLinksRequest(MsgToSend msgToSend) {
    this.msg = msgToSend;
  }

  public List<SmartLink> getSmartLinks() {
    return smartLinks;
  }

  public void setSmartLinks(List<SmartLink> smartLinks) {
    this.smartLinks = smartLinks;
  }

  public MsgToSend getMsg() {
    return msg;
  }

}

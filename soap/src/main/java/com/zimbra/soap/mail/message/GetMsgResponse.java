// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.MsgWithGroupInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_MSG_RESPONSE)
public class GetMsgResponse {

  /**
   * @zm-api-field-description Message information
   */
  @XmlElement(name = MailConstants.E_MSG /* m */, required = false)
  private MsgWithGroupInfo msg;

  public GetMsgResponse() {}

  public void setMsg(MsgWithGroupInfo msg) {
    this.msg = msg;
  }

  public MsgWithGroupInfo getMsg() {
    return msg;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper.add("msg", msg);
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}

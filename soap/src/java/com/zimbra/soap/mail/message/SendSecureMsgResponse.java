// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.soap.mail.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.SmimeConstants;
import com.zimbra.soap.mail.message.SendMsgResponse;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=SmimeConstants.E_SEND_SECURE_MSG_RESPONSE)
public class SendSecureMsgResponse extends SendMsgResponse {

}

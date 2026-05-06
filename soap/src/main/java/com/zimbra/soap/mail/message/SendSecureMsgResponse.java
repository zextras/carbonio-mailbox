// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.SmimeConstants;
import com.zimbra.soap.mail.message.SendMsgResponse;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=SmimeConstants.E_SEND_SECURE_MSG_RESPONSE)
public class SendSecureMsgResponse extends SendMsgResponse {

}

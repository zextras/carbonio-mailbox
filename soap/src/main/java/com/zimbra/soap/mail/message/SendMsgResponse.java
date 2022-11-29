// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.MsgWithGroupInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_SEND_MSG_RESPONSE)
public class SendMsgResponse {

    /**
     * @zm-api-field-description Message Information about the saved copy of the sent message.
     * Note, "m" element will have no content if the message was not saved.
     * Note, Full information will be provided if fetchSavedMsg was specified in the request, otherwise
     * only the message id will be returned.
     */
    @XmlElement(name=MailConstants.E_MSG /* m */, required=false)
    private MsgWithGroupInfo msg;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    public SendMsgResponse() {
    }

    public MsgWithGroupInfo getMsg() { return msg; }

    public void setSavedMessage(MsgWithGroupInfo msg) { this.msg = msg; }

    public MsgWithGroupInfo getSavedMsg() { return msg; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("msg", msg);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

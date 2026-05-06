// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ChatSummary;
import com.zimbra.soap.mail.type.MessageSummary;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_ADD_MSG_RESPONSE)
public class AddMsgResponse {

    /**
     * @zm-api-field-description Details of added message
     */
    @XmlElements({
        @XmlElement(name=MailConstants.E_CHAT /* chat */, type=ChatSummary.class),
        @XmlElement(name=MailConstants.E_MSG /* m */, type=MessageSummary.class)
    })
    private MessageSummary message;

    public AddMsgResponse() {
    }

    public AddMsgResponse(MessageSummary message) {
        setMessage(message);
    }

    public void setMessage(MessageSummary message) {
        this.message = message;
    }

    public MessageSummary getMessage() {
        return message;
    }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("message", message);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}

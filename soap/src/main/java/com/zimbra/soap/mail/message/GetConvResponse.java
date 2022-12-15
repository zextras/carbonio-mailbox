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
import com.zimbra.soap.mail.type.ConversationInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_CONV_RESPONSE)
public class GetConvResponse {

    /**
     * @zm-api-field-description Conversation information
     */
    @XmlElement(name=MailConstants.E_CONV, required=true)
    private final ConversationInfo conversation;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetConvResponse() {
        this((ConversationInfo) null);
    }

    public GetConvResponse(ConversationInfo conversation) {
        this.conversation = conversation;
    }

    public ConversationInfo getConversation() { return conversation; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("conversation", conversation)
            .toString();
    }
}

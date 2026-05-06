// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ChatSummary;
import com.zimbra.soap.mail.type.MessageSummary;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_MSG_METADATA_RESPONSE)
public class GetMsgMetadataResponse {

    /**
     * @zm-api-field-description Message metadata
     */
    @XmlElements({
        @XmlElement(name=MailConstants.E_CHAT /* chat */, type=ChatSummary.class),
        @XmlElement(name=MailConstants.E_MSG /* m */, type=MessageSummary.class)
    })
    private List<MessageSummary> messages = Lists.newArrayList();

    public GetMsgMetadataResponse() {
    }

    public void setMessages(Iterable <MessageSummary> messages) {
        this.messages.clear();
        if (messages != null) {
            Iterables.addAll(this.messages,messages);
        }
    }

    public void addMessage(MessageSummary message) {
        this.messages.add(message);
    }

    public List<MessageSummary> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("messages", messages);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

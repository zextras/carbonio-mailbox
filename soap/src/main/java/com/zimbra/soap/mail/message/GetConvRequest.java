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
import com.zimbra.soap.mail.type.ConversationSpec;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get Conversation
 * <br />
 * GetConvRequest gets information about the 1 conversation named by id's value.
 * It will return exactly 1 conversation element.
 * <br />
 * <br />
 * If fetch="1|all" is included, the full expanded message structure is inlined for the first (or for all) messages
 * in the conversation.  If fetch="{item-id}", only the message with the given {item-id} is expanded inline.
 * <br />
 * if headers are requested, any matching headers are inlined into the response (not available when raw="1")
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_CONV_REQUEST)
public class GetConvRequest {

    /**
     * @zm-api-field-description Conversation specification
     */
    @XmlElement(name=MailConstants.E_CONV /* c */, required=true)
    @ZimbraUniqueElement
    private final ConversationSpec conversation;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetConvRequest() {
        this((ConversationSpec) null);
    }

    public GetConvRequest(ConversationSpec conversation) {
        this.conversation = conversation;
    }

    public ConversationSpec getConversation() { return conversation; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("conversation", conversation)
            .toString();
    }
}

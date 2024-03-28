// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_MIMEPART)
public class MimePartAttachSpec extends AttachSpec {

    /**
     * @zm-api-field-tag message-id
     * @zm-api-field-description Message ID
     */
    @XmlAttribute(name=MailConstants.A_MESSAGE_ID, required=true)
    private final String messageId;

    /**
     * @zm-api-field-tag part
     * @zm-api-field-description Part
     */
    @XmlAttribute(name=MailConstants.A_PART, required=true)
    private final String part;

    public void setRequiresSmartLinkConversion(Boolean requiresSmartLinkConversion) {
        this.requiresSmartLinkConversion = requiresSmartLinkConversion;
    }

    @XmlAttribute(name=MailConstants.A_REQUIRES_SMART_LINK_CONVERSION, required=true)
    private Boolean requiresSmartLinkConversion;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private MimePartAttachSpec() {
        this((String) null, (String) null);
    }

    public MimePartAttachSpec(String messageId, String part) {
        this.messageId = messageId;
        this.part = part;
    }

    public MimePartAttachSpec(String messageId, String part, Boolean requiresSmartLinkConversion) {
        this.messageId = messageId;
        this.part = part;
        this.requiresSmartLinkConversion = requiresSmartLinkConversion;
    }

    public String getMessageId() { return messageId; }
    public String getPart() { return part; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("messageId", messageId)
            .add("part", part);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }

    public Boolean getRequiresSmartLinkConversion() {
        return requiresSmartLinkConversion;
    }
}

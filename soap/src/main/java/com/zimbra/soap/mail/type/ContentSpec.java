// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ContentSpec {

    /**
     * @zm-api-field-tag attachment-upload-id
     * @zm-api-field-description Attachment upload ID of uploaded object to use
     */
    @XmlAttribute(name=MailConstants.A_ATTACHMENT_ID /* aid */, required=false)
    private String attachmentId;

    /**
     * @zm-api-field-tag message-id
     * @zm-api-field-description Message ID of existing message. Used in conjunction with "part"
     */
    @XmlAttribute(name=MailConstants.A_MESSAGE_ID /* mid */, required=false)
    private String messageId;

    /**
     * @zm-api-field-tag part-identifier
     * @zm-api-field-description Part identifier.  This combined with "mid" identifies a part of an existing message
     */
    @XmlAttribute(name=MailConstants.A_PART /* part */, required=false)
    private String part;

    /**
     * @zm-api-field-tag inlined-content
     * @zm-api-field-description Inlined content data.  Ignored if "aid" or "mid"/"part" specified
     */
    @XmlValue
    private String text;

    public ContentSpec() {
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public void setPart(String part) { this.part = part; }
    public void setText(String text) { this.text = text; }
    public String getAttachmentId() { return attachmentId; }
    public String getMessageId() { return messageId; }
    public String getPart() { return part; }
    public String getText() { return text; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("attachmentId", attachmentId)
            .add("messageId", messageId)
            .add("part", part)
            .add("text", text);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

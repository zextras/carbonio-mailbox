// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"mimeParts", "attachments"})
public class MimePartInfo {

    /**
     * @zm-api-field-tag content-type
     * @zm-api-field-description Content type
     */
    @XmlAttribute(name=MailConstants.A_CONTENT_TYPE /* ct */, required=false)
    private String contentType;

    /**
     * @zm-api-field-tag content
     * @zm-api-field-description Content
     */
    @XmlAttribute(name=MailConstants.E_CONTENT /* content */, required=false)
    private String content;

    /**
     * @zm-api-field-tag content-id
     * @zm-api-field-description Content ID
     */
    @XmlAttribute(name=MailConstants.A_CONTENT_ID /* ci */, required=false)
    private String contentId;

    /**
     * @zm-api-field-description MIME Parts
     */
    @XmlElement(name=MailConstants.E_MIMEPART /* mp */, required=false)
    private final List<MimePartInfo> mimeParts = Lists.newArrayList();

    /**
     * @zm-api-field-description Attachments
     */
    @XmlElement(name=MailConstants.E_ATTACH /* attach */, required=false)
    private AttachmentsInfo attachments;

    public MimePartInfo() {
    }

    public static MimePartInfo createForContentType(String ct) {
        final MimePartInfo mp = new MimePartInfo();
        mp.setContentType(ct);
        return mp;
    }

    public static MimePartInfo createForContentTypeAndContent(String ct, String text) {
        final MimePartInfo mp = createForContentType(ct);
        mp.setContent(text);
        return mp;
    }

    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setContent(String content) { this.content = content; }
    public void setContentId(String contentId) { this.contentId = contentId; }
    public void setMimeParts(Iterable <MimePartInfo> mimeParts) {
        this.mimeParts.clear();
        if (mimeParts != null) {
            Iterables.addAll(this.mimeParts,mimeParts);
        }
    }

    public void addMimePart(MimePartInfo mimePart) {
        this.mimeParts.add(mimePart);
    }

    public void setAttachments(AttachmentsInfo attachments) { this.attachments = attachments; }
    public String getContentType() { return contentType; }
    public String getContent() { return content; }
    public String getContentId() { return contentId; }
    public List<MimePartInfo> getMimeParts() {
        return mimeParts;
    }
    public AttachmentsInfo getAttachments() { return attachments; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("contentType", contentType)
            .add("content", content)
            .add("contentId", contentId)
            .add("mimeParts", mimeParts)
            .add("attachments", attachments);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ConversationMsgHitInfo {

    /**
     * @zm-api-field-tag message-id
     * @zm-api-field-description Message ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-tag size
     * @zm-api-field-description Size
     */
    @XmlAttribute(name=MailConstants.A_SIZE /* s */, required=false)
    private Long size;

    /**
     * @zm-api-field-tag folder-id
     * @zm-api-field-description Folder ID
     */
    @XmlAttribute(name=MailConstants.A_FOLDER /* l */, required=false)
    private String folderId;

    /**
     * @zm-api-field-tag flags
     * @zm-api-field-description flags
     */
    @XmlAttribute(name=MailConstants.A_FLAGS /* f */, required=false)
    private String flags;

    /**
     * @zm-api-field-tag auto-send-time
     * @zm-api-field-description Can optionally set autoSendTime to specify the time at which the draft should be
     * automatically sent by the server
     */
    @XmlAttribute(name=MailConstants.A_AUTO_SEND_TIME /* autoSendTime */, required=false)
    private Long autoSendTime;

    /**
     * @zm-api-field-tag date
     * @zm-api-field-description date
     */
    @XmlAttribute(name=MailConstants.A_DATE /* d */, required=false)
    private Long date;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ConversationMsgHitInfo() {
        this(null);
    }

    public ConversationMsgHitInfo(String id) {
        this.id = id;
    }

    public static ConversationMsgHitInfo fromIdAndFolderId(String id, String fId) {
        final ConversationMsgHitInfo hit = new ConversationMsgHitInfo(id);
        hit.setFolderId(fId);
        return hit;
    }

    public void setSize(Long size) { this.size = size; }
    public void setFolderId(String folderId) { this.folderId = folderId; }
    public void setFlags(String flags) { this.flags = flags; }
    public void setAutoSendTime(Long autoSendTime) {
        this.autoSendTime = autoSendTime;
    }
    public void setDate(Long date) { this.date = date; }
    public String getId() { return id; }
    public Long getSize() { return size; }
    public String getFolderId() { return folderId; }
    public String getFlags() { return flags; }
    public Long getAutoSendTime() { return autoSendTime; }
    public Long getDate() { return date; }

    /** Done like this rather than using JAXB for performance reasons */
    public Element toElement(Element parent) {
        final Element mel = parent.addNonUniqueElement(MailConstants.E_MSG).addAttribute(MailConstants.A_ID, id);
        if (size != null) {
            mel.addAttribute(MailConstants.A_SIZE, size);
        }
        if (folderId != null) {
            mel.addAttribute(MailConstants.A_FOLDER, folderId);
        }
        if (flags != null) {
            mel.addAttribute(MailConstants.A_FLAGS, flags);
        }
        if (autoSendTime != null) {
            mel.addAttribute(MailConstants.A_AUTO_SEND_TIME, autoSendTime);
        }
        if (date != null) {
            mel.addAttribute(MailConstants.A_DATE, date);
        }
        return mel;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("size", size)
            .add("folderId", folderId)
            .add("flags", flags)
            .add("autoSendTime", autoSendTime)
            .add("date", date);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

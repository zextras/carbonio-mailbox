// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.voice.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class VoiceFolderSummary {

    /**
     * @zm-api-field-tag folder-id
     * @zm-api-field-description Folder ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private String folderId;

    /**
     * @zm-api-field-tag unread-count
     * @zm-api-field-description Unread count
     */
    @XmlAttribute(name=MailConstants.A_UNREAD /* u */, required=true)
    private Long unreadCount;

    /**
     * @zm-api-field-tag msg-count
     * @zm-api-field-description Message count
     */
    @XmlAttribute(name=MailConstants.A_NUM /* n */, required=true)
    private Long msgCount;

    public VoiceFolderSummary() {
    }

    public void setFolderId(String folderId) { this.folderId = folderId; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
    public void setMsgCount(Long msgCount) { this.msgCount = msgCount; }
    public String getFolderId() { return folderId; }
    public Long getUnreadCount() { return unreadCount; }
    public Long getMsgCount() { return msgCount; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("folderId", folderId)
            .add("unreadCount", unreadCount)
            .add("msgCount", msgCount);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class AttachmentIdAttrib {

    /**
     * @zm-api-field-tag attachment-id
     * @zm-api-field-description Attachment ID
     */
    @XmlAttribute(name=AdminConstants.A_ATTACHMENT_ID /* aid */, required=false)
    private String attachmentId;

    public AttachmentIdAttrib() {
    }

    public AttachmentIdAttrib(String attachmentId) {
        this.setAttachmentId(attachmentId);
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getAttachmentId() { return attachmentId; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("attachmentId", attachmentId);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}

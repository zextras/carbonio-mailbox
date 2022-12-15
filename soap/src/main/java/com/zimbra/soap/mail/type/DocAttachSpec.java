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

// See ParseMimeMessage.handleAttachments

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_DOC)
public class DocAttachSpec
extends AttachSpec {

    /**
     * @zm-api-field-tag document-path
     * @zm-api-field-description Document path.  If specified "id" and "ver" attributes are ignored
     */
    @XmlAttribute(name=MailConstants.A_PATH /* path */, required=false)
    private String path;

    /**
     * @zm-api-field-tag item-id
     * @zm-api-field-description Item ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private String id;

    /**
     * @zm-api-field-tag version
     * @zm-api-field-description Optional Version.
     */
    @XmlAttribute(name=MailConstants.A_VERSION /* ver */, required=false)
    private Integer version;

    public DocAttachSpec() {
    }

    public void setPath(String path) { this.path = path; }
    public void setId(String id) { this.id = id; }
    public void setVersion(Integer version) { this.version = version; }
    public String getPath() { return path; }
    public String getId() { return id; }
    public Integer getVersion() { return version; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("path", path)
            .add("id", id)
            .add("version", version);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

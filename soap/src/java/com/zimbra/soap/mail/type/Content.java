// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="content")
public class Content {

    /**
     * @zm-api-field-tag attachment-upload-id
     * @zm-api-field-description Attachment upload ID of uploaded object to use
     */
    @XmlAttribute(required=false, name="aid") private String attachUploadId;

    /**
     * @zm-api-field-tag inlined-content
     * @zm-api-field-description Inlined content data.  Ignored if "aid" is specified
     */
    @XmlValue private String value;

    public Content() {
    }

    public String getAttachUploadId() {
        return attachUploadId;
    }

    public void setAttachUploadId(String attachUploadId) {
        this.attachUploadId = attachUploadId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

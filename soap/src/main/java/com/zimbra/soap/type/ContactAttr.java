// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class ContactAttr extends KeyValuePair {

    // part/contentType/size/contentFilename are required when
    // encoding attachments

    /**
     * @zm-api-field-tag contact-part-id
     * @zm-api-field-description Part ID.
     * <br />
     * Can only specify a <b>{contact-part-id}</b> when modifying an existent contact
     */
    @XmlAttribute(name=MailConstants.A_PART /* part */, required=false)
    private String part;

    /**
     * @zm-api-field-tag contact-content-type
     * @zm-api-field-description Content type
     */
    @XmlAttribute(name=MailConstants.A_CONTENT_TYPE /* ct */, required=false)
    private String contentType;

    /**
     * @zm-api-field-tag contact-size
     * @zm-api-field-description Size
     */
    @XmlAttribute(name=MailConstants.A_SIZE /* s */, required=false)
    private Integer size;

    /**
     * @zm-api-field-tag contact-content-filename
     * @zm-api-field-description Content filename
     */
    @XmlAttribute(name=MailConstants.A_CONTENT_FILENAME /* filename */, required=false)
    private String contentFilename;

    public ContactAttr() {
    }

    public void setPart(String part) { this.part = part; }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setSize(Integer size) { this.size = size; }
    public void setContentFilename(String contentFilename) {
        this.contentFilename = contentFilename;
    }

    public String getPart() { return part; }
    public String getContentType() { return contentType; }
    public Integer getSize() { return size; }
    public String getContentFilename() { return contentFilename; }
}

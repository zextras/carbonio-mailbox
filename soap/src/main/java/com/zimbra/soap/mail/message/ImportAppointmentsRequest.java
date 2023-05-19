// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.ContentSpec;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Import appointments
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_IMPORT_APPOINTMENTS_REQUEST)
public class ImportAppointmentsRequest {

    /**
     * @zm-api-field-tag folder-id
     * @zm-api-field-description Optional folder ID to import appointments into
     */
    @XmlAttribute(name=MailConstants.A_FOLDER /* l */, required=false)
    private String folderId;

    /**
     * @zm-api-field-tag content-type
     * @zm-api-field-description Content type
     * <br />
     * Only currently supported content type is "text/calendar" (and its nickname "ics")
     */
    @XmlAttribute(name=MailConstants.A_CONTENT_TYPE /* ct */, required=true)
    private final String contentType;

    /**
     * @zm-api-field-description Content specification
     */
    @XmlElement(name=MailConstants.E_CONTENT /* content */, required=true)
    private final ContentSpec content;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ImportAppointmentsRequest() {
        this(null, null);
    }

    public ImportAppointmentsRequest(String contentType, ContentSpec content) {
        this.contentType = contentType;
        this.content = content;
    }

    public void setFolderId(String folderId) { this.folderId = folderId; }
    public String getFolderId() { return folderId; }
    public String getContentType() { return contentType; }
    public ContentSpec getContent() { return content; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("folderId", folderId)
            .add("contentType", contentType)
            .add("content", content);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.calendar.Attach;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.CalendarAttachInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class CalendarAttach implements CalendarAttachInterface {

    /**
     * @zm-api-field-tag alarm-attach-uri
     * @zm-api-field-description URI
     */
    @XmlAttribute(name=MailConstants.A_CAL_ATTACH_URI /* uri */, required=false)
    private String uri;

    /**
     * @zm-api-field-tag alarm-attach-content-type
     * @zm-api-field-description Content Type for <b>{base64-encoded-binary-alarm-attach-data}</b>
     */
    @XmlAttribute(name=MailConstants.A_CAL_ATTACH_CONTENT_TYPE /* ct */, required=false)
    private String contentType;

    /**
     * @zm-api-field-tag base64-encoded-binary-alarm-attach-data
     * @zm-api-field-description Base64 encoded binary alarrm attach data
     */
    @XmlValue
    private String binaryB64Data;

    public CalendarAttach() {
    }

    public CalendarAttach(Attach att) {
        this.uri = att.getUri();
        if (this.uri != null) {
            this.contentType = att.getContentType();
        } else {
            this.binaryB64Data = att.getBinaryB64Data();
        }
    }

    @Override
    public CalendarAttachInterface createFromAttach(Attach att) {
        return new CalendarAttach(att);
    }

    @Override
    public void setUri(String uri) { this.uri = uri; }
    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public void setBinaryB64Data(String binaryB64Data) {
        this.binaryB64Data = binaryB64Data;
    }

    @Override
    public String getUri() { return uri; }
    @Override
    public String getContentType() { return contentType; }
    @Override
    public String getBinaryB64Data() { return binaryB64Data; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("uri", uri)
            .add("contentType", contentType)
            .add("binaryB64Data", binaryB64Data);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}

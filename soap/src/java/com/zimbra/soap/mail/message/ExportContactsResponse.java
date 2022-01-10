// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_EXPORT_CONTACTS_RESPONSE)
public class ExportContactsResponse {

    /**
     * @zm-api-field-tag export-content
     * @zm-api-field-description Content of the export
     */
    @XmlElement(name=MailConstants.E_CONTENT /* content */, required=true)
    private final String content;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ExportContactsResponse() {
        this((String) null);
    }

    public ExportContactsResponse(String content) {
        this.content = content;
    }

    public String getContent() { return content; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("content", content);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

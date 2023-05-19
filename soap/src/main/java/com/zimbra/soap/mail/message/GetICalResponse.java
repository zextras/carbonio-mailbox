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
import com.zimbra.soap.mail.type.ICalContent;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="GetICalResponse")
public class GetICalResponse {

    /**
     * @zm-api-field-description iCalendar content
     */
    @XmlElement(name=MailConstants.E_CAL_ICAL /* ical */, required=true)
    private final ICalContent content;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetICalResponse() {
        this(null);
    }

    public GetICalResponse(ICalContent content) {
        this.content = content;
    }

    public ICalContent getContent() { return content; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("content", content);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

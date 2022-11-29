// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.CalendarItemInfo;
import com.zimbra.soap.mail.type.TaskItemInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name="GetTaskResponse")
public class GetTaskResponse {

    /**
     * @zm-api-field-description Task information
     */
    @XmlElements({
        @XmlElement(name=MailConstants.E_APPOINTMENT /* appt */, type=CalendarItemInfo.class),
        @XmlElement(name=MailConstants.E_TASK /* task */, type=TaskItemInfo.class)
    })
    private final CalendarItemInfo item;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetTaskResponse() {
        this((CalendarItemInfo) null);
    }

    public GetTaskResponse(CalendarItemInfo item) {
        this.item = item;
    }

    public CalendarItemInfo getItem() { return item; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("item", item);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

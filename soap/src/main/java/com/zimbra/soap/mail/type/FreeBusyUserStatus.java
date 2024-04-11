// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.zimbra.common.soap.MailConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class FreeBusyUserStatus {

    /**
     * @zm-api-field-tag email
     * @zm-api-field-description Email address for a user who has a conflict with the instance
     */
    @XmlAttribute(name=MailConstants.A_NAME /* name */, required=true)
    private final String name;

    /**
     * @zm-api-field-tag freebusy-status-B|T|O
     * @zm-api-field-description Free/Busy status - <b>B|T|O</b> (Busy, Tentative or Out-of-office)
     */
    @XmlAttribute(name=MailConstants.A_APPT_FREEBUSY /* fb */, required=true)
    private final String freebusyStatus;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private FreeBusyUserStatus() {
        this(null, null);
    }

    public FreeBusyUserStatus(String name, String freebusyStatus) {
        this.name = name;
        this.freebusyStatus = freebusyStatus;
    }

    public String getName() { return name; }
    public String getFreebusyStatus() { return freebusyStatus; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("name", name)
            .add("freebusyStatus", freebusyStatus);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

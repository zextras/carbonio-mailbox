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
import com.zimbra.soap.base.DateTimeStringAttrInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class DateTimeStringAttr
implements DateTimeStringAttrInterface {

    /**
     * @zm-api-field-tag YYYYMMDD[ThhmmssZ]
     * @zm-api-field-description Date in format : YYYYMMDD[ThhmmssZ]
     */
    @XmlAttribute(name=MailConstants.A_CAL_DATETIME, required=true)
    private final String dateTime;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DateTimeStringAttr() {
        this(null);
    }

    public DateTimeStringAttr(String dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public DateTimeStringAttrInterface create(String dateTime) {
        return new DateTimeStringAttr(dateTime);
    }

    @Override
    public String getDateTime() { return dateTime; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("dateTime", dateTime)
            .toString();
    }
}

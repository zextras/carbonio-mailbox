// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlValue;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class CalendarResourceSelector {

    @XmlEnum
    public enum CalendarResourceBy {
        // case must match protocol
        id, foreignPrincipal, name;

        public static CalendarResourceBy fromString(String s) throws ServiceException {
            try {
                return CalendarResourceBy.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw ServiceException.INVALID_REQUEST("unknown key: "+s, e);
            }
        }
        public com.zimbra.common.account.Key.CalendarResourceBy toKeyCalendarResourceBy()
        throws ServiceException {
            return com.zimbra.common.account.Key.CalendarResourceBy.fromString(this.name());
        }
    }

    /**
     * @zm-api-field-tag cal-resource-selector-by
     * @zm-api-field-description Select the meaning of <b>{cal-resource-selector-key}</b>
     */
    @XmlAttribute(name=AdminConstants.A_BY)
    private final
    CalendarResourceBy calResourceBy;

    /**
     * @zm-api-field-tag cal-resource-selector-key
     * @zm-api-field-description The key used to identify the account. Meaning determined by
     * <b>{cal-resource-selector-by}</b>
     */
    @XmlValue
    private final String key;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CalendarResourceSelector() {
        this.calResourceBy = null;
        this.key = null;
    }

    public CalendarResourceSelector(CalendarResourceBy by, String key) {
        this.calResourceBy = by;
        this.key = key;
    }

    public static CalendarResourceSelector fromId(String id) {
        return new CalendarResourceSelector(CalendarResourceBy.id, id);
    }

    public static CalendarResourceSelector fromName(String name) {
        return new CalendarResourceSelector(CalendarResourceBy.name, name);
    }

    public String getKey() { return key; }

    public CalendarResourceBy getBy() { return calResourceBy; }
}

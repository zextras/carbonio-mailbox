// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.admin.type.CalendarResourceInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_RENAME_CALENDAR_RESOURCE_RESPONSE)
public class RenameCalendarResourceResponse {

    /**
     * @zm-api-field-description Information about calendar resource
     */
    @XmlElement(name=AdminConstants.E_CALENDAR_RESOURCE, required=true)
    private final CalendarResourceInfo calResource;

    /**
     * no-argument constructor wanted by JAXB
     */
     @SuppressWarnings("unused")
    private RenameCalendarResourceResponse() {
        this(null);
    }

    public RenameCalendarResourceResponse(CalendarResourceInfo calResource) {
        this.calResource = calResource;
    }

    public CalendarResourceInfo getCalResource() { return calResource; }
}

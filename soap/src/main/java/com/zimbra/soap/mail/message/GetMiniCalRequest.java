// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.CalTZInfo;
import com.zimbra.soap.type.Id;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Get information needed for Mini Calendar.
 * <br />
 * Date is returned if there is at least one appointment on that date.  The date computation uses the requesting
 * (authenticated) account's time zone, not the time zone of the account that owns the calendar folder.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_MINI_CAL_REQUEST)
@XmlType(propOrder = {"folders", "timezone"})
public class GetMiniCalRequest {

    /**
     * @zm-api-field-tag range-start-time-millis
     * @zm-api-field-description Range start time in milliseconds
     */
    @XmlAttribute(name=MailConstants.A_CAL_START_TIME /* s */, required=true)
    private final long startTime;

    /**
     * @zm-api-field-tag range-end-time-millis
     * @zm-api-field-description Range end time in milliseconds
     */
    @XmlAttribute(name=MailConstants.A_CAL_END_TIME /* e */, required=true)
    private final long endTime;

    /**
     * @zm-api-field-description Local and/or remote calendar folders
     */
    @XmlElement(name=MailConstants.E_FOLDER /* folder */, required=false)
    private List<Id> folders = Lists.newArrayList();

    /**
     * @zm-api-field-description Optional timezone specifier.  References an existing server-known timezone by ID or
     * the full specification of a custom timezone
     */
    @XmlElement(name=MailConstants.E_CAL_TZ /* tz */, required=false)
    private CalTZInfo timezone;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private GetMiniCalRequest() {
        this(-1L, -1L);
    }

    public GetMiniCalRequest(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void setFolders(Iterable <Id> folders) {
        this.folders.clear();
        if (folders != null) {
            Iterables.addAll(this.folders,folders);
        }
    }

    public GetMiniCalRequest addFolder(Id folder) {
        this.folders.add(folder);
        return this;
    }

    public void setTimezone(CalTZInfo timezone) { this.timezone = timezone; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public List<Id> getFolders() {
        return Collections.unmodifiableList(folders);
    }
    public CalTZInfo getTimezone() { return timezone; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("startTime", startTime)
            .add("endTime", endTime)
            .add("folders", folders)
            .add("timezone", timezone);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

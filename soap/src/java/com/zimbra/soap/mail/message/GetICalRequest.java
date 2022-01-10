// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Retrieve the unparsed (but XML-encoded (&amp;quot)) iCalendar data for an Invite
 * <br />
 * This is intended for interfacing with 3rd party programs
 * <br />
 * <ul>
 * <li> If <b>id</b> attribute specified, gets the iCalendar representation for one invite
 * <li> If <b>id</b> attribute is <b>not</b> specified, then start/end MUST be, Calendar data is returned for entire
 *      specified range
 * </ul>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_GET_ICAL_REQUEST)
public class GetICalRequest {

    /**
     * @zm-api-field-tag invite-msg-id
     * @zm-api-field-description If specified, gets the iCalendar representation for one invite
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private String id;

    /**
     * @zm-api-field-tag range-start-millis-gmt
     * @zm-api-field-description Range start in milliseconds
     */
    @XmlAttribute(name=MailConstants.A_CAL_START_TIME /* s */, required=false)
    private Long startTime;

    /**
     * @zm-api-field-tag range-end-millis-gmt
     * @zm-api-field-description Range end in milliseconds
     */
    @XmlAttribute(name=MailConstants.A_CAL_END_TIME /* e */, required=false)
    private Long endTime;

    public GetICalRequest() {
    }

    public void setId(String id) { this.id = id; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }
    public void setEndTime(Long endTime) { this.endTime = endTime; }
    public String getId() { return id; }
    public Long getStartTime() { return startTime; }
    public Long getEndTime() { return endTime; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("startTime", startTime)
            .add("endTime", endTime);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}

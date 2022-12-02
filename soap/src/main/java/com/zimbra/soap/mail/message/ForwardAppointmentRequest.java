// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.Msg;
import com.zimbra.soap.mail.type.CalTZInfo;
import com.zimbra.soap.mail.type.DtTimeInfo;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Used by an attendee to forward an instance or entire appointment to another user who
 * is not already an attendee.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_FORWARD_APPOINTMENT_REQUEST)
public class ForwardAppointmentRequest {

    /**
     * @zm-api-field-tag appointment-item-id
     * @zm-api-field-description Appointment item ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=false)
    private String id;

    /**
     * @zm-api-field-description RECURRENCE-ID information if forwarding a single instance of a recurring appointment
     */
    @XmlElement(name=MailConstants.E_CAL_EXCEPTION_ID /* exceptId */, required=false)
    private DtTimeInfo exceptionId;

    /**
     * @zm-api-field-description Definition for TZID referenced by DATETIME in <b>&lt;exceptId></b>
     */
    @XmlElement(name=MailConstants.E_CAL_TZ /* tz */, required=false)
    private CalTZInfo timezone;

    // E_INVITE child is not allowed
    /**
     * @zm-api-field-description Details of the appointment
     */
    @XmlElement(name=MailConstants.E_MSG /* m */, required=false)
    private Msg msg;

    public ForwardAppointmentRequest() {
    }

    public void setId(String id) { this.id = id; }
    public void setExceptionId(DtTimeInfo exceptionId) {
        this.exceptionId = exceptionId;
    }
    public void setTimezone(CalTZInfo timezone) { this.timezone = timezone; }
    public void setMsg(Msg msg) { this.msg = msg; }
    public String getId() { return id; }
    public DtTimeInfo getExceptionId() { return exceptionId; }
    public CalTZInfo getTimezone() { return timezone; }
    public Msg getMsg() { return msg; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("exceptionId", exceptionId)
            .add("timezone", timezone)
            .add("msg", msg);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

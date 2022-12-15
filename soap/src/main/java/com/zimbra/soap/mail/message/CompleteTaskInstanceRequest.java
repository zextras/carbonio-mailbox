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

import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.CalTZInfo;
import com.zimbra.soap.mail.type.DtTimeInfo;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Complete a task instance
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_COMPLETE_TASK_INSTANCE_REQUEST)
public class CompleteTaskInstanceRequest {

    /**
     * @zm-api-field-tag id
     * @zm-api-field-description ID
     */
    @XmlAttribute(name=AccountConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-description Exception ID
     */
    @XmlElement(name=MailConstants.E_CAL_EXCEPTION_ID /* exceptId */, required=true)
    private final DtTimeInfo exceptionId;

    /**
     * @zm-api-field-description Timezone information
     */
    @XmlElement(name=MailConstants.E_CAL_TZ /* tz */, required=false)
    private CalTZInfo timezone;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CompleteTaskInstanceRequest() {
        this((String) null, (DtTimeInfo) null);
    }

    public CompleteTaskInstanceRequest(String id, DtTimeInfo exceptionId) {
        this.id = id;
        this.exceptionId = exceptionId;
    }

    public void setTimezone(CalTZInfo timezone) { this.timezone = timezone; }
    public String getId() { return id; }
    public DtTimeInfo getExceptionId() { return exceptionId; }
    public CalTZInfo getTimezone() { return timezone; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("exceptionId", exceptionId)
            .add("timezone", timezone);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

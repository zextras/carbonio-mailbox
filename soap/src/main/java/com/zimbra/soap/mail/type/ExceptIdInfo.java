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
public class ExceptIdInfo {

    /**
     * @zm-api-field-tag recurrence_id_of_exception
     * @zm-api-field-description Recurrence ID of exception
     */
    @XmlAttribute(name=MailConstants.A_CAL_RECURRENCE_ID /* recurId */, required=true)
    private final String recurrenceId;

    /**
     * @zm-api-field-tag invite-id-of-exception
     * @zm-api-field-description Invite ID of exception
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private ExceptIdInfo() {
        this((String) null, (String) null);
    }

    public ExceptIdInfo(String recurrenceId, String id) {
        this.recurrenceId = recurrenceId;
        this.id = id;
    }

    public String getRecurrenceId() { return recurrenceId; }
    public String getId() { return id; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("recurrenceId", recurrenceId)
            .add("id", id);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

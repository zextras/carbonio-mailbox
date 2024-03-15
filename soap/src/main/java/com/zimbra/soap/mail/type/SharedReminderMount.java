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
import com.zimbra.soap.type.ZmBoolean;

@XmlAccessorType(XmlAccessType.NONE)
public class SharedReminderMount {

    /**
     * @zm-api-field-tag mountpoint-id
     * @zm-api-field-description Mountpoint ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-tag show-reminders-flag
     * @zm-api-field-description Set to enable (or unset to disable) reminders for shared appointments/tasks
     */
    @XmlAttribute(name=MailConstants.A_REMINDER /* reminder */, required=false)
    private final ZmBoolean showReminders;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private SharedReminderMount() {
        this(null, null);
    }

    public SharedReminderMount(String id, Boolean showReminders) {
        this.id = id;
        this.showReminders = ZmBoolean.fromBool(showReminders);
    }

    public String getId() { return id; }
    public Boolean getShowReminders() { return ZmBoolean.toBool(showReminders); }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("showReminders", showReminders);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

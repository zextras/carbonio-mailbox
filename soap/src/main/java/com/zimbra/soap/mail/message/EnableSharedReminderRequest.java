// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.SharedReminderMount;
import com.zimbra.soap.json.jackson.annotate.ZimbraUniqueElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description Enable/disable reminders for shared appointments/tasks on a mountpoint
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=MailConstants.E_ENABLE_SHARED_REMINDER_REQUEST)
public class EnableSharedReminderRequest {

    /**
     * @zm-api-field-description Specification for mountpoint
     */
    @ZimbraUniqueElement
    @XmlElement(name=MailConstants.E_MOUNT /* link */, required=true)
    private final SharedReminderMount mount;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private EnableSharedReminderRequest() {
        this(null);
    }

    public EnableSharedReminderRequest(SharedReminderMount mount) {
        this.mount = mount;
    }

    public SharedReminderMount getMount() { return mount; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("mount", mount)
            .toString();
    }
}

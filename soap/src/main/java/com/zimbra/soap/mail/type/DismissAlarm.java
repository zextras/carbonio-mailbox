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
public class DismissAlarm {

    /**
     * @zm-api-field-tag cal-item-id
     * @zm-api-field-description Calendar item ID
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-tag dismissed-at-millis
     * @zm-api-field-description Time alarm was dismissed, in millis
     */
    @XmlAttribute(name=MailConstants.A_CAL_ALARM_DISMISSED_AT /* dismissedAt */, required=true)
    private final long dismissedAt;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    protected DismissAlarm() {
        this(null, -1L);
    }

    public DismissAlarm(String id, long dismissedAt) {
        this.id = id;
        this.dismissedAt = dismissedAt;
    }

    public String getId() { return id; }
    public long getDismissedAt() { return dismissedAt; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("dismissedAt", dismissedAt);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

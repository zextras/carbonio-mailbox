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
public class LegacyInstanceDataAttrs
extends CommonInstanceDataAttrs {

    // MailConstants.A_CAL_DURATION == "d" == MailConstants.A_DATE.
    // This eclipses the date setting - hence the existence of
    // InstanceDataAttrs which uses A_CAL_NEW_DURATION
    /**
     * @zm-api-field-tag duration
     * @zm-api-field-description Duration
     */
    @XmlAttribute(name=MailConstants.A_CAL_DURATION /* "d" */, required=false)
    private Long duration;

    public LegacyInstanceDataAttrs() {
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
    public Long getDuration() { return duration; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        helper = super.addToStringInfo(helper);
        return helper
            .add("duration", duration);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

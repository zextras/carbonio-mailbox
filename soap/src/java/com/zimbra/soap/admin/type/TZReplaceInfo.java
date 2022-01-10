// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.type.Id;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class TZReplaceInfo {

    // TZID from /opt/zextras/conf/timezones.ics
    /**
     * @zm-api-field-tag well-known-tzid
     * @zm-api-field-description TZID from /opt/zextras/conf/timezones.ics
     */
    @XmlElement(name=AdminConstants.E_WELL_KNOWN_TZ /* wellKnownTz */, required=false)
    private Id wellKnownTz;

    /**
     * @zm-api-field-description Timezone
     */
    @XmlElement(name=MailConstants.E_CAL_TZ /* tz */, required=false)
    private CalTZInfo calTz;

    public TZReplaceInfo() {
    }

    public void setWellKnownTz(Id wellKnownTz) { this.wellKnownTz = wellKnownTz; }
    public void setCalTz(CalTZInfo calTz) { this.calTz = calTz; }
    public Id getWellKnownTz() { return wellKnownTz; }
    public CalTZInfo getCalTz() { return calTz; }

    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("wellKnownTz", wellKnownTz)
            .add("calTz", calTz);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}

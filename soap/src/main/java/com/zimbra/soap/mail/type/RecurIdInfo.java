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
import com.zimbra.soap.base.RecurIdInfoInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class RecurIdInfo implements RecurIdInfoInterface {

    /**
     * @zm-api-field-tag range-type
     * @zm-api-field-description Recurrence range type
     */
    @XmlAttribute(name=MailConstants.A_CAL_RECURRENCE_RANGE_TYPE /* rangeType */, required=true)
    private int recurrenceRangeType;

    /**
     * @zm-api-field-tag YYMMDD[THHMMSS[Z]]
     * @zm-api-field-description Recurrence ID in format : YYMMDD[THHMMSS[Z]]
     */
    @XmlAttribute(name=MailConstants.A_CAL_RECURRENCE_ID /* recurId */, required=true)
    private String recurrenceId;

    /**
     * @zm-api-field-tag timezone-name
     * @zm-api-field-description Timezone name
     */
    @XmlAttribute(name=MailConstants.A_CAL_TIMEZONE /* tz */, required=false)
    private String timezone;

    /**
     * @zm-api-field-tag YYMMDDTHHMMSSZ
     * @zm-api-field-description Recurrence-id in UTC time zone; used in non-all-day appointments only
     * <br />
     * Format: YYMMDDTHHMMSSZ
     */
    @XmlAttribute(name=MailConstants.A_CAL_RECURRENCE_ID_Z /* ridZ */, required=false)
    private String recurIdZ;

    public RecurIdInfo() {
        this(-1, null);
    }

    public RecurIdInfo(int recurrenceRangeType, String recurrenceId) {
        this.setRecurrenceRangeType(recurrenceRangeType);
        this.setRecurrenceId(recurrenceId);
    }

    @Override
    public RecurIdInfoInterface createFromRangeTypeAndId(
            int recurrenceRangeType, String recurrenceId) {
        return new RecurIdInfo(recurrenceRangeType, recurrenceId);
    }

    @Override
    public void setRecurrenceRangeType(int recurrenceRangeType) {
        this.recurrenceRangeType = recurrenceRangeType;
    }

    @Override
    public void setRecurrenceId( String recurrenceId) {
        this.recurrenceId = recurrenceId;
    }

    @Override
    public void setTimezone(String timezone) { this.timezone = timezone; }
    @Override
    public void setRecurIdZ(String recurIdZ) { this.recurIdZ = recurIdZ; }

    @Override
    public int getRecurrenceRangeType() { return recurrenceRangeType; }
    @Override
    public String getRecurrenceId() { return recurrenceId; }
    @Override
    public String getTimezone() { return timezone; }
    @Override
    public String getRecurIdZ() { return recurIdZ; }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("recurrenceRangeType", getRecurrenceRangeType())
            .add("recurrenceId", getRecurrenceId())
            .add("timezone", timezone)
            .add("recurIdZ", recurIdZ);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

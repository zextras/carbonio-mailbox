// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.MoreObjects;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.DtTimeInfoInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class DtTimeInfo
implements DtTimeInfoInterface {

    /**
     * @zm-api-field-tag YYYYMMDD['T'HHMMSS[Z]]
     * @zm-api-field-description Date and/or time.  Format is : <b>YYYYMMDD['T'HHMMSS[Z]]</b>
     * <br />
     * where:
     * <pre>
     *     YYYY - 4 digit year
     *     MM   - 2 digit month
     *     DD   - 2 digit day
     * Optionally:
     *     'T' the literal char "T" then
     *     HH - 2 digit hour (00-23)
     *     MM - 2 digit minute (00-59)
     *     SS - 2 digit second (00-59)
     *     ...and finally an optional "Z" meaning that the time is UTC,
     *     otherwise the tz="TIMEZONE" param MUST be specified with the DATETIME
     *     e.g:
     *         20050612  June 12, 2005
     *         20050315T18302305Z  March 15, 2005 6:30:23.05 PM UTC
     * </pre>
     */
    @XmlAttribute(name=MailConstants.A_CAL_DATETIME /* d */, required=false)
    private final String dateTime;

    /**
     * @zm-api-field-tag timezone-identifier
     * @zm-api-field-description Java timezone identifier
     */
    @XmlAttribute(name=MailConstants.A_CAL_TIMEZONE /* tz */, required=false)
    private String timezone;

    /**
     * @zm-api-field-tag utc-time
     * @zm-api-field-description UTC time as milliseconds since the epoch.  Set if non-all-day
     */
    @XmlAttribute(name=MailConstants.A_CAL_DATETIME_UTC /* u */, required=false)
    private Long utcTime;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private DtTimeInfo() {
        this(null);
    }

    public DtTimeInfo(String dateTime) {
        this.dateTime = dateTime;
    }

    public static DtTimeInfoInterface create(String dateTime) {
        return new DtTimeInfo(dateTime);
    }

    public static DtTimeInfo createForDatetimeAndZone(String dt, String tz) {
        DtTimeInfo dti = new DtTimeInfo(dt);
        dti.setTimezone(tz);
        return dti;
    }

    @Override
    public void setTimezone(String timezone) { this.timezone = timezone; }
    @Override
    public void setUtcTime(Long utcTime) { this.utcTime = utcTime; }
    @Override
    public String getDateTime() { return dateTime; }
    @Override
    public String getTimezone() { return timezone; }
    @Override
    public Long getUtcTime() { return utcTime; }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("dateTime", dateTime)
            .add("timezone", timezone)
            .add("utcTime", utcTime)
            .toString();
    }
}

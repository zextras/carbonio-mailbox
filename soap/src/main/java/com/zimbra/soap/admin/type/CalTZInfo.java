// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.CalTZInfoInterface;
import com.zimbra.soap.type.TzOnsetInfo;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {})
public class CalTZInfo implements CalTZInfoInterface {

    /**
     * @zm-api-field-tag timezone-id
     * @zm-api-field-description Timezone ID.
     * If this is the only detail present then this should be an existing server-known timezone's ID
     * Otherwise, it must be present, although it will be ignored by the server
     */
    @XmlAttribute(name=MailConstants.A_ID /* id */, required=true)
    private final String id;

    /**
     * @zm-api-field-tag timezone-std-offset
     * @zm-api-field-description Standard Time's offset in minutes from UTC; local = UTC + offset
     */
    @XmlAttribute(name=MailConstants.A_CAL_TZ_STDOFFSET /* stdoff */, required=true)
    private final Integer tzStdOffset;

    /**
     * @zm-api-field-tag timezone-daylight-offset
     * @zm-api-field-description Daylight Saving Time's offset in minutes from UTC; present only if DST is used
     */
    @XmlAttribute(name=MailConstants.A_CAL_TZ_DAYOFFSET /* dayoff */, required=true)
    private final Integer tzDayOffset;

    /**
     * @zm-api-field-description Time/rule for transitioning from daylight time to standard time.
     * Either specify week/wkday combo, or mday.
     */
    @XmlElement(name=MailConstants.E_CAL_TZ_STANDARD /* standard */, required=false)
    private TzOnsetInfo standardTzOnset;

    /**
     * @zm-api-field-description Time/rule for transitioning from standard time to daylight time
     */
    @XmlElement(name=MailConstants.E_CAL_TZ_DAYLIGHT /* daylight */, required=false)
    private TzOnsetInfo daylightTzOnset;

    /**
     * @zm-api-field-description Standard Time component's timezone name
     */
    @XmlAttribute(name=MailConstants.A_CAL_TZ_STDNAME /* stdname */, required=false)
    private String standardTZName;

    /**
     * @zm-api-field-description Daylight Saving Time component's timezone name
     */
    @XmlAttribute(name=MailConstants.A_CAL_TZ_DAYNAME /* dayname */, required=false)
    private String daylightTZName;

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private CalTZInfo() {
        this(null, null, null);
    }

    public CalTZInfo(String id, Integer tzStdOffset, Integer tzDayOffset) {
        this.id = id;
        this.tzStdOffset = tzStdOffset;
        this.tzDayOffset = tzDayOffset;
    }

    @Override
    public CalTZInfoInterface createFromIdStdOffsetDayOffset(String id,
            Integer tzStdOffset, Integer tzDayOffset) {
        return new CalTZInfo(id, tzStdOffset, tzDayOffset);
    }

    @Override
    public void setStandardTzOnset(TzOnsetInfo standardTzOnset) {
        this.standardTzOnset = standardTzOnset;
    }

    @Override
    public void setDaylightTzOnset(TzOnsetInfo daylightTzOnset) {
        this.daylightTzOnset = daylightTzOnset;
    }

    @Override
    public void setStandardTZName(String standardTZName) {
        this.standardTZName = standardTZName;
    }

    @Override
    public void setDaylightTZName(String daylightTZName) {
        this.daylightTZName = daylightTZName;
    }

    @Override
    public String getId() { return id; }
    @Override
    public Integer getTzStdOffset() { return tzStdOffset; }
    @Override
    public Integer getTzDayOffset() { return tzDayOffset; }
    @Override
    public TzOnsetInfo getStandardTzOnset() { return standardTzOnset; }
    @Override
    public TzOnsetInfo getDaylightTzOnset() { return daylightTzOnset; }
    @Override
    public String getStandardTZName() { return standardTZName; }
    @Override
    public String getDaylightTZName() { return daylightTZName; }

    public static Iterable <CalTZInfo> fromInterfaces(Iterable <CalTZInfoInterface> params) {
        if (params == null)
            return null;
        List <CalTZInfo> newList = Lists.newArrayList();
        for (CalTZInfoInterface param : params) {
            newList.add((CalTZInfo) param);
        }
        return newList;
    }

    public static List <CalTZInfoInterface> toInterfaces(Iterable <CalTZInfo> params) {
        if (params == null)
            return null;
        List <CalTZInfoInterface> newList = Lists.newArrayList();
        Iterables.addAll(newList, params);
        return newList;
    }
    public MoreObjects.ToStringHelper addToStringInfo(
                MoreObjects.ToStringHelper helper) {
        return helper
            .add("id", id)
            .add("tzStdOffset", tzStdOffset)
            .add("tzDayOffset", tzDayOffset)
            .add("standardTzOnset", standardTzOnset)
            .add("daylightTzOnset", daylightTzOnset)
            .add("standardTZName", standardTZName)
            .add("daylightTZName", daylightTZName);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this))
                .toString();
    }
}

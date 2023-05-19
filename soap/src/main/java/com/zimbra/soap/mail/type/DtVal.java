// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.List;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.MailConstants;

import com.zimbra.soap.base.DtTimeInfoInterface;
import com.zimbra.soap.base.DtValInterface;
import com.zimbra.soap.base.DurationInfoInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class DtVal implements DtValInterface {

    /**
     * @zm-api-field-description Start DATE-TIME
     */
    @XmlElement(name=MailConstants.E_CAL_START_TIME /* s */, required=false)
    private DtTimeInfo startTime;

    /**
     * @zm-api-field-description Start DATE-TIME
     */
    @XmlElement(name=MailConstants.E_CAL_END_TIME /* e */, required=false)
    private DtTimeInfo endTime;

    /**
     * @zm-api-field-description Duration information
     */
    @XmlElement(name=MailConstants.E_CAL_DURATION /* dur */, required=false)
    private DurationInfo duration;

    public DtVal() {
    }

    public void setStartTime(DtTimeInfo startTime) { this.startTime = startTime; }
    public void setEndTime(DtTimeInfo endTime) { this.endTime = endTime; }
    public void setDuration(DurationInfo duration) { this.duration = duration; }
    public DtTimeInfo getStartTime() { return startTime; }
    public DtTimeInfo getEndTime() { return endTime; }
    public DurationInfo getDuration() { return duration; }

    @Override
    public DtTimeInfoInterface getStartTimeInterface() { return startTime; }
    @Override
    public DtTimeInfoInterface getEndTimeInterface() { return endTime; }
    @Override
    public DurationInfoInterface getDurationInterface() { return duration; }
    @Override
    public void setStartTimeInterface(DtTimeInfoInterface endTime) { setStartTime(startTime); }
    @Override
    public void setEndTimeInterface(DtTimeInfoInterface endTime) { setEndTime((DtTimeInfo) endTime); }
    @Override
    public void setDurationInterface(DurationInfoInterface duration) { setDuration((DurationInfo) duration); }

    public static Iterable <DtVal> fromInterfaces(Iterable <DtValInterface> params) {
        if (params == null)
            return null;
        List <DtVal> newList = Lists.newArrayList();
        for (DtValInterface param : params) {
            newList.add((DtVal) param);
        }
        return newList;
    }

    public static List <DtValInterface> toInterfaces(Iterable <DtVal> params) {
        if (params == null)
            return null;
        List <DtValInterface> newList = Lists.newArrayList();
        for (DtVal param : params) {
            newList.add(param);
        }
        return newList;
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("startTime", startTime)
            .add("endTime", endTime)
            .add("duration", duration);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.base.DtValInterface;
import com.zimbra.soap.base.SingleDatesInterface;

@XmlAccessorType(XmlAccessType.NONE)
public class SingleDates
implements RecurRuleBase, SingleDatesInterface {

    /**
     * @zm-api-field-tag TZID
     * @zm-api-field-description TZID
     */
    @XmlAttribute(name=MailConstants.A_CAL_TIMEZONE /* tz */, required=false)
    private String timezone;

    /**
     * @zm-api-field-description Information on start date/time and end date/time or duration
     */
    @XmlElement(name=MailConstants.E_CAL_DATE_VAL /* dtval */, required=false)
    private List<DtVal> dtVals = Lists.newArrayList();

    public SingleDates() {
    }

    public void setTimezone(String timezone) { this.timezone = timezone; }
    public void setDtvals(Iterable <DtVal> dtVals) {
        this.dtVals.clear();
        if (dtVals != null) {
            Iterables.addAll(this.dtVals,dtVals);
        }
    }

    public void addDtval(DtVal dtVal) {
        this.dtVals.add(dtVal);
    }

    public String getTimezone() { return timezone; }
    public List<DtVal> getDtvals() {
        return dtVals;
    }

    @Override
    public void setDtValInterfaces(Iterable<DtValInterface> dtVals) {
        setDtvals(DtVal.fromInterfaces(dtVals));
    }

    @Override
    public void addDtValInterface(DtValInterface dtVal) {
        addDtval((DtVal) dtVal);
    }

    @Override
    public List<DtValInterface> getDtValInterfaces() {
        return DtVal.toInterfaces(dtVals);
    }

    public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
        return helper
            .add("timezone", timezone)
            .add("dtVals", dtVals);
    }

    @Override
    public String toString() {
        return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
    }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zimbra.common.calendar.TZIDMapper;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ZDateTime implements ToZJSONObject {

    private static final String FMT_DATE = "yyyyMMdd";
    private static final String FMT_DATE_TIME = "yyyyMMdd'T'HHmmss";

    private String mDateTime;
    private String mTimeZoneId;

	public ZDateTime(Element e) throws ServiceException {
        mDateTime = e.getAttribute(MailConstants.A_CAL_DATETIME);
        mTimeZoneId = e.getAttribute(MailConstants.A_CAL_TIMEZONE, null);
    }

    public Element toElement(String name, Element parent) {
        Element dtEl = parent.addElement(name);
        dtEl.addAttribute(MailConstants.A_CAL_DATETIME, mDateTime);
        if (mTimeZoneId != null)
            dtEl.addAttribute(MailConstants.A_CAL_TIMEZONE, mTimeZoneId);
        return dtEl;
    }

	public Date getDate() {
        DateFormat df =  new SimpleDateFormat((mDateTime.indexOf('T') == -1) ? FMT_DATE : FMT_DATE_TIME);
        df.setLenient(false);
        TimeZone tz = null;
        if (mTimeZoneId != null && mTimeZoneId.length() > 0) {
            tz = TimeZone.getTimeZone(TZIDMapper.canonicalize(mTimeZoneId));
            df.setTimeZone(tz);
        } else if (mDateTime.indexOf('Z') != -1) {
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        Date date = null;

        try {
            date = df.parse(mDateTime);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }

	@Override
    public ObjectNode toObjectNode() {
        ObjectNode zjo = JsonNodeFactory.instance.objectNode();
        zjo.put("time", getDate().getTime());
        if (mDateTime != null) zjo.put("dateTime", mDateTime);
        if (mTimeZoneId != null) zjo.put("timeZoneId", mTimeZoneId);
        return zjo;
    }

    public String toString() {
        return String.format("[ZDateTime %s]", getDate().toString());
    }

}

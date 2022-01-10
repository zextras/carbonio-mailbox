// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.calendar;

import com.zimbra.common.calendar.ZCalendar.ICalTok;
import com.zimbra.common.calendar.ZCalendar.ZProperty;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;

/**
 * iCalendar GEO property
 */
public class Geo {

    private String mLatitude;
    private String mLongitude;

    public String getLatitude()  { return mLatitude; }
    public String getLongitude() { return mLongitude; }

    public Geo(String lat, String lon) {
        mLatitude = lat;
        mLongitude = lon;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Geo))
            return false;
        Geo other = (Geo) o;
        return
            mLatitude != null && mLatitude.equals(other.mLatitude) &&
            mLongitude != null && mLongitude.equals(other.mLongitude);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mLatitude).append(";").append(mLongitude);
        return sb.toString();
    }

    public Element toXml(Element parent) {
        Element geo = parent.addElement(MailConstants.E_CAL_GEO);
        geo.addAttribute(MailConstants.A_CAL_GEO_LATITUDE, mLatitude);
        geo.addAttribute(MailConstants.A_CAL_GEO_LONGITUDE, mLongitude);
        return geo;
    }

    public static Geo parse(Element geoElem) {
        String latitude = geoElem.getAttribute(MailConstants.A_CAL_GEO_LATITUDE, "0");
        String longitude = geoElem.getAttribute(MailConstants.A_CAL_GEO_LONGITUDE, "0");
        return new Geo(latitude, longitude);
    }

    public ZProperty toZProperty() {
        ZProperty prop = new ZProperty(ICalTok.GEO);
        prop.setValue(mLatitude + ";" + mLongitude);
        return prop;
    }

    public static Geo parse(ZProperty prop) {
        String val = prop.getValue();
        String[] latlon = val.split(";");
        if (latlon != null && latlon.length == 2)
            return new Geo(latlon[0], latlon[1]);
        else
            return new Geo("0", "0");    
    }
}

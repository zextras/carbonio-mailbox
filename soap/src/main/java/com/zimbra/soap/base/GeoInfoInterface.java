// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.zimbra.common.calendar.Geo;

@XmlAccessorType(XmlAccessType.NONE)
public interface GeoInfoInterface {
    GeoInfoInterface create(String latitude, String longitude);
    GeoInfoInterface create(Geo geo);
    String getLatitude();
    String getLongitude();
}

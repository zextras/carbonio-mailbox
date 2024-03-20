// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.List;

public interface InstanceDataInterface
extends CommonInstanceDataAttrsInterface {
    void setStartTime(Long startTime);
    void setIsException(Boolean isException);
    void setOrganizer(CalOrganizer organizer);
    void setCategories(Iterable<String> categories);
    void addCategory(String category);
    void setGeo(GeoInfo geo);
    void setFragment(String fragment);
    Long getStartTime();
    Boolean getIsException();
    CalOrganizer getOrganizer();
    List<String> getCategories();
    GeoInfo getGeo();
    String getFragment();
}

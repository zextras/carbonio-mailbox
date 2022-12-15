// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.List;

public interface InstanceDataInterface
extends CommonInstanceDataAttrsInterface {
    public void setStartTime(Long startTime);
    public void setIsException(Boolean isException);
    public void setOrganizer(CalOrganizer organizer);
    public void setCategories(Iterable <String> categories);
    public void addCategory(String category);
    public void setGeo(GeoInfo geo);
    public void setFragment(String fragment);
    public Long getStartTime();
    public Boolean getIsException();
    public CalOrganizer getOrganizer();
    public List<String> getCategories();
    public GeoInfo getGeo();
    public String getFragment();
}

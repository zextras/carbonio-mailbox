// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.List;

public interface CalendaringDataInterface
extends CommonInstanceDataAttrsInterface {
    void setDate(Long date);
    void setOrganizer(CalOrganizer organizer);
    void setCategories(Iterable<String> categories);
    void addCategory(String category);
    void setGeo(GeoInfo geo);
    void setFragment(String fragment);
    // used in interface instead of methods related to JAXB field
    void setCalendaringInstances(
        Iterable<InstanceDataInterface> instances);
    // used in interface instead of methods related to JAXB field
    void addCalendaringInstance(InstanceDataInterface instance);
    void setAlarmData(AlarmDataInfo alarmData);

    Long getDate();
    CalOrganizer getOrganizer();
    List<String> getCategories();
    GeoInfo getGeo();
    String getFragment();
    // used in interface instead of methods related to JAXB field
    List<InstanceDataInterface> getCalendaringInstances();
    AlarmDataInfo getAlarmData();

    // see CommonCalendaringData
    void setFlags(String flags);
    void setTags(String tags);
    void setFolderId(String folderId);
    void setSize(Long size);
    void setChangeDate(Long changeDate);
    void setModifiedSequence(Integer modifiedSequence);
    void setRevision(Integer revision);
    void setId(String id);

    String getXUid();
    String getUid();
    String getFlags();
    String getTags();
    String getFolderId();
    Long getSize();
    Long getChangeDate();
    Integer getModifiedSequence();
    Integer getRevision();
    String getId();
}

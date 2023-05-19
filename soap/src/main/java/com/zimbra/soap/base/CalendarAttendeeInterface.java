// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface CalendarAttendeeInterface {
    void setAddress(String address);
    void setUrl(String url);
    void setDisplayName(String displayName);
    void setSentBy(String sentBy);
    void setDir(String dir);
    void setLanguage(String language);
    void setCuType(String cuType);
    void setRole(String role);
    void setPartStat(String partStat);
    void setRsvp(Boolean rsvp);
    void setMember(String member);
    void setDelegatedTo(String delegatedTo);
    void setDelegatedFrom(String delegatedFrom);

    String getAddress();
    String getUrl();
    String getDisplayName();
    String getSentBy();
    String getDir();
    String getLanguage();
    String getCuType();
    String getRole();
    String getPartStat();
    Boolean getRsvp();
    String getMember();
    String getDelegatedTo();
    String getDelegatedFrom();

    void setXParamInterfaces(Iterable<XParamInterface> xParams);
    void addXParamInterface(XParamInterface xParam);
    List<XParamInterface> getXParamInterfaces();
}

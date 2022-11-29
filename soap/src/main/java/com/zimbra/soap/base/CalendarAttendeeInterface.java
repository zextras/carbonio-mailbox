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
    public void setAddress(String address);
    public void setUrl(String url);
    public void setDisplayName(String displayName);
    public void setSentBy(String sentBy);
    public void setDir(String dir);
    public void setLanguage(String language);
    public void setCuType(String cuType);
    public void setRole(String role);
    public void setPartStat(String partStat);
    public void setRsvp(Boolean rsvp);
    public void setMember(String member);
    public void setDelegatedTo(String delegatedTo);
    public void setDelegatedFrom(String delegatedFrom);

    public String getAddress();
    public String getUrl();
    public String getDisplayName();
    public String getSentBy();
    public String getDir();
    public String getLanguage();
    public String getCuType();
    public String getRole();
    public String getPartStat();
    public Boolean getRsvp();
    public String getMember();
    public String getDelegatedTo();
    public String getDelegatedFrom();

    public void setXParamInterfaces(Iterable<XParamInterface> xParams);
    public void addXParamInterface(XParamInterface xParam);
    public List<XParamInterface> getXParamInterfaces();
}

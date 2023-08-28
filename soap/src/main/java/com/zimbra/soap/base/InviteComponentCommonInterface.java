// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface InviteComponentCommonInterface {
  public InviteComponentCommonInterface create(String method, int componentNum, boolean rsvp);

  public void setPriority(String priority);

  public void setName(String name);

  public void setLocation(String location);

  public void setNoBlob(Boolean noBlob);

  public void setFreeBusyActual(String freeBusyActual);

  public void setFreeBusy(String freeBusy);

  public void setTransparency(String transparency);

  public void setIsOrganizer(Boolean isOrganizer);

  public void setXUid(String xUid);

  public void setUid(String uid);

  public void setSequence(Integer sequence);

  public void setDateTime(Long dateTime);

  public void setCalItemId(String calItemId);

  public void setDeprecatedApptId(String deprecatedApptId);

  public void setCalItemFolder(String calItemFolder);

  public void setStatus(String status);

  public void setCalClass(String calClass);

  public void setUrl(String url);

  public void setIsException(Boolean isException);

  public void setRecurIdZ(String recurIdZ);

  public void setIsAllDay(Boolean isAllDay);

  public void setIsDraft(Boolean isDraft);

  public void setNeverSent(Boolean neverSent);

  public void setChanges(String changes);

  public String getMethod();

  public int getComponentNum();

  public boolean getRsvp();

  public String getPriority();

  public String getName();

  public String getLocation();

  public Boolean getNoBlob();

  public String getFreeBusyActual();

  public String getFreeBusy();

  public String getTransparency();

  public Boolean getIsOrganizer();

  public String getXUid();

  public String getUid();

  public Integer getSequence();

  public Long getDateTime();

  public String getCalItemId();

  public String getDeprecatedApptId();

  public String getCalItemFolder();

  public String getStatus();

  public String getCalClass();

  public String getUrl();

  public Boolean getIsException();

  public String getRecurIdZ();

  public Boolean getIsAllDay();

  public Boolean getIsDraft();

  public Boolean getNeverSent();

  public String getChanges();
}

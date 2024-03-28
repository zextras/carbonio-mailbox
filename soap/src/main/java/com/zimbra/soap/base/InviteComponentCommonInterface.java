// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface InviteComponentCommonInterface {
  InviteComponentCommonInterface create(String method, int componentNum, boolean rsvp);

  void setPriority(String priority);

  void setName(String name);

  void setLocation(String location);

  void setNoBlob(Boolean noBlob);

  void setFreeBusyActual(String freeBusyActual);

  void setFreeBusy(String freeBusy);

  void setTransparency(String transparency);

  void setIsOrganizer(Boolean isOrganizer);

  void setXUid(String xUid);

  void setUid(String uid);

  void setSequence(Integer sequence);

  void setDateTime(Long dateTime);

  void setCalItemId(String calItemId);

  void setDeprecatedApptId(String deprecatedApptId);

  void setCalItemFolder(String calItemFolder);

  void setStatus(String status);

  void setCalClass(String calClass);

  void setUrl(String url);

  void setIsException(Boolean isException);

  void setRecurIdZ(String recurIdZ);

  void setIsAllDay(Boolean isAllDay);

  void setIsDraft(Boolean isDraft);

  void setNeverSent(Boolean neverSent);

  void setChanges(String changes);

  String getMethod();

  int getComponentNum();

  boolean getRsvp();

  String getPriority();

  String getName();

  String getLocation();

  Boolean getNoBlob();

  String getFreeBusyActual();

  String getFreeBusy();

  String getTransparency();

  Boolean getIsOrganizer();

  String getXUid();

  String getUid();

  Integer getSequence();

  Long getDateTime();

  String getCalItemId();

  String getDeprecatedApptId();

  String getCalItemFolder();

  String getStatus();

  String getCalClass();

  String getUrl();

  Boolean getIsException();

  String getRecurIdZ();

  Boolean getIsAllDay();

  Boolean getIsDraft();

  Boolean getNeverSent();

  String getChanges();
}

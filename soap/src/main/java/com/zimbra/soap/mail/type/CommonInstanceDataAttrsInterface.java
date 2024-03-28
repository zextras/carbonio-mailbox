// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

public interface CommonInstanceDataAttrsInterface {
  void setPartStat(String partStat);

  void setRecurIdZ(String recurIdZ);

  void setTzOffset(Long tzOffset);

  void setFreeBusyActual(String freeBusyActual);

  void setIsRecurring(Boolean isRecurring);

  void setPriority(String priority);

  void setFreeBusyIntended(String freeBusyIntended);

  void setTransparency(String transparency);

  void setName(String name);

  void setLocation(String location);

  void setHasOtherAttendees(Boolean hasOtherAttendees);

  void setHasAlarm(Boolean hasAlarm);

  void setIsOrganizer(Boolean isOrganizer);

  void setInvId(String invId);

  void setComponentNum(Integer componentNum);

  void setStatus(String status);

  void setCalClass(String calClass);

  void setAllDay(Boolean allDay);

  void setDraft(Boolean draft);

  void setNeverSent(Boolean neverSent);

  // see CommonInstanceDataAttrs
  String getPartStat();

  String getRecurIdZ();

  Long getTzOffset();

  String getFreeBusyActual();

  Boolean getIsRecurring();

  String getPriority();

  String getFreeBusyIntended();

  String getTransparency();

  String getName();

  String getLocation();

  Boolean getHasOtherAttendees();

  Boolean getHasAlarm();

  Boolean getIsOrganizer();

  String getInvId();

  Integer getComponentNum();

  String getStatus();

  String getCalClass();

  Boolean getAllDay();

  Boolean getDraft();

  Boolean getNeverSent();

  // see InstanceDataAttrs /LegacyInstanceDataAttrs
  void setDuration(Long duration);

  Long getDuration();
}

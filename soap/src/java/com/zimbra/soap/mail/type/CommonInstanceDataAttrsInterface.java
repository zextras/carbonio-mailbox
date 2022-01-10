// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

public interface CommonInstanceDataAttrsInterface {
    public void setPartStat(String partStat);
    public void setRecurIdZ(String recurIdZ);
    public void setTzOffset(Long tzOffset);
    public void setFreeBusyActual(String freeBusyActual);
    public void setTaskPercentComplete(String taskPercentComplete);
    public void setIsRecurring(Boolean isRecurring);
    public void setPriority(String priority);
    public void setFreeBusyIntended(String freeBusyIntended);
    public void setTransparency(String transparency);
    public void setName(String name);
    public void setLocation(String location);
    public void setHasOtherAttendees(Boolean hasOtherAttendees);
    public void setHasAlarm(Boolean hasAlarm);
    public void setIsOrganizer(Boolean isOrganizer);
    public void setInvId(String invId);
    public void setComponentNum(Integer componentNum);
    public void setStatus(String status);
    public void setCalClass(String calClass);
    public void setAllDay(Boolean allDay);
    public void setDraft(Boolean draft);
    public void setNeverSent(Boolean neverSent);
    public void setTaskDueDate(Long taskDueDate);
    public void setTaskTzOffsetDue(Integer taskTzOffsetDue);

    // see CommonInstanceDataAttrs
    public String getPartStat();
    public String getRecurIdZ();
    public Long getTzOffset();
    public String getFreeBusyActual();
    public String getTaskPercentComplete();
    public Boolean getIsRecurring();
    public String getPriority();
    public String getFreeBusyIntended();
    public String getTransparency();
    public String getName();
    public String getLocation();
    public Boolean getHasOtherAttendees();
    public Boolean getHasAlarm();
    public Boolean getIsOrganizer();
    public String getInvId();
    public Integer getComponentNum();
    public String getStatus();
    public String getCalClass();
    public Boolean getAllDay();
    public Boolean getDraft();
    public Boolean getNeverSent();
    public Long getTaskDueDate();
    public Integer getTaskTzOffsetDue();

    // see InstanceDataAttrs /LegacyInstanceDataAttrs
    public void setDuration(Long duration);
    public Long getDuration();
}

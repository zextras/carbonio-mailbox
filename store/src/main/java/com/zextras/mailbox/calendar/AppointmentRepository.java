// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.calendar;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Appointment;
import com.zimbra.cs.mailbox.Appointment.CalendarMetadata;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.util.TypedIdList;
import java.util.List;

public interface AppointmentRepository {

  void copyCalendar(Appointment calItem, int newId, boolean fromDumpster) throws ServiceException;

  UnderlyingData getCalendar(Mailbox mbox, String uid) throws ServiceException;

  List<UnderlyingData> getCalendars(Mailbox mbox, long start, long end, int folderId, int[] excludeFolderIds) throws ServiceException;

  List<UnderlyingData> getCalendars(Mailbox mbox, List<String> uids) throws ServiceException;

  TypedIdList listCalendars(Mailbox mbox, long start, long end, int folderId, int[] excludeFolderIds) throws ServiceException;

  void addCalendar(Appointment calItem) throws ServiceException;

  void updateCalendar(Appointment calItem) throws ServiceException;

  List<CalendarMetadata> getCalendarMetadata(Folder folder, long start, long end) throws ServiceException;
}

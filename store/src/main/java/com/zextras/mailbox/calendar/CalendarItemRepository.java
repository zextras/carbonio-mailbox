// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.calendar;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.UnderlyingData;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.util.TypedIdList;
import java.util.List;

public interface CalendarItemRepository {

  void copyCalendarItem(CalendarItem calItem, int newId, boolean fromDumpster) throws ServiceException;

  UnderlyingData getCalendarItem(Mailbox mbox, String uid) throws ServiceException;

  List<UnderlyingData> getCalendarItems(Mailbox mbox, MailItem.Type type, long start, long end, int folderId, int[] excludeFolderIds) throws ServiceException;

  List<UnderlyingData> getCalendarItems(Mailbox mbox, List<String> uids) throws ServiceException;

  TypedIdList listCalendarItems(Mailbox mbox, MailItem.Type type, long start, long end, int folderId, int[] excludeFolderIds) throws ServiceException;

  void addToCalendarItemTable(CalendarItem calItem) throws ServiceException;

  void updateInCalendarItemTable(CalendarItem calItem) throws ServiceException;

  List<CalendarItem.CalendarMetadata> getCalendarItemMetadata(Folder folder, long start, long end) throws ServiceException;
}

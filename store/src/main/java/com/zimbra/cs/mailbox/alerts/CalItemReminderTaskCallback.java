// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.alerts;

import com.zimbra.common.util.ScheduledTaskCallback;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.ScheduledTaskResult;
import java.util.concurrent.Callable;

/**
 * @author vmahajan
 */
public class CalItemReminderTaskCallback implements ScheduledTaskCallback<ScheduledTaskResult> {

  public void afterTaskRun(Callable<ScheduledTaskResult> task, ScheduledTaskResult lastResult) {
    if (lastResult == null) return;
    if (task instanceof CalItemReminderTaskBase) {
      ZimbraLog.scheduler.debug("afterTaskRun() for %s", task);
      CalItemReminderService.scheduleNextReminders(
          (CalendarItem) lastResult,
          task instanceof CalItemEmailReminderTask,
          task instanceof CalItemSmsReminderTask);
    }
  }
}

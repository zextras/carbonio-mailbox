// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.calendar.Invite;
import com.zimbra.cs.redolog.op.CreateCalendarItemPlayer;
import com.zimbra.cs.redolog.op.CreateCalendarItemRecorder;
import javax.mail.internet.MimeMessage;

public class Task extends CalendarItem {
  public Task(Mailbox mbox, UnderlyingData data) throws ServiceException {
    this(mbox, data, false);
  }

  public Task(Mailbox mbox, UnderlyingData data, boolean skipCache) throws ServiceException {
    super(mbox, data, skipCache);
  }

  @Override
  protected String processPartStat(
      Invite invite, MimeMessage mmInv, boolean forCreate, String defaultPartStat)
      throws ServiceException {
    Mailbox mbox = getMailbox();
    OperationContext octxt = mbox.getOperationContext();
    CreateCalendarItemPlayer player =
        octxt != null ? (CreateCalendarItemPlayer) octxt.getPlayer() : null;

    String partStat = defaultPartStat;
    if (player != null) {
      String p = player.getCalendarItemPartStat();
      if (p != null) partStat = p;
    }

    CreateCalendarItemRecorder recorder = (CreateCalendarItemRecorder) mbox.getRedoRecorder();
    recorder.setCalendarItemPartStat(partStat);

    Account account = getMailbox().getAccount();
    invite.updateMyPartStat(account, partStat);
    if (forCreate) {
      Invite defaultInvite = getDefaultInviteOrNull();
      if (defaultInvite != null
          && !defaultInvite.equals(invite)
          && !partStat.equals(defaultInvite.getPartStat())) {
        defaultInvite.updateMyPartStat(account, partStat);
        saveMetadata();
      }
    }
    return partStat;
  }
}

// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail.message.parser;

import com.zimbra.common.calendar.ZCalendar;
import com.zimbra.cs.mailbox.calendar.Invite;

public class InviteParserResult {

  public ZCalendar.ZVCalendar mCal;
  public String mUid;
  public String mSummary;
  public Invite mInvite;
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import com.zimbra.soap.base.AddRecurrenceInfoInterface;

public class AddRecurrenceInfo extends RecurrenceInfo
    implements AddRecurrenceInfoInterface, RecurRuleBase {
  public static AddRecurrenceInfo create(SimpleRepeatingRule rule) {
    AddRecurrenceInfo info = new AddRecurrenceInfo();
    info.addRule(rule);
    return info;
  }
}

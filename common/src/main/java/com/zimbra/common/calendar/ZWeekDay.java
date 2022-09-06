// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.calendar;

import java.util.Calendar;

public enum ZWeekDay {
  FR,
  MO,
  SA,
  SU,
  TH,
  TU,
  WE;

  public int getCalendarDay() {
    switch (this) {
      case SU:
        return Calendar.SUNDAY;
      case MO:
        return Calendar.MONDAY;
      case TU:
        return Calendar.TUESDAY;
      case WE:
        return Calendar.WEDNESDAY;
      case TH:
        return Calendar.THURSDAY;
      case FR:
        return Calendar.FRIDAY;
    }
    return Calendar.SATURDAY;
  }
}

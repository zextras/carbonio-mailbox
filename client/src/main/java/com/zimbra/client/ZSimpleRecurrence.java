// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.client.ZInvite.ZByDayWeekDay;
import com.zimbra.client.ZInvite.ZWeekDay;

import java.util.List;

public class ZSimpleRecurrence {

    public enum ZSimpleRecurrenceType {
        NONE,
        DAILY,
        DAILY_WEEKDAY,
        DAILY_INTERVAL,
        WEEKLY,
        WEEKLY_BY_DAY,
        WEEKLY_CUSTOM,
        MONTHLY,
        MONTHLY_BY_MONTH_DAY,
        MONTHLY_RELATIVE,
        YEARLY,
        YEARLY_BY_DATE,
        YEARLY_RELATIVE,
        COMPLEX;

    }

    public enum ZSimpleRecurrenceEnd {
        NEVER, COUNT, UNTIL;

		}


    private ZSimpleRecurrenceType mType;
    private ZSimpleRecurrenceEnd mEnd;
    private long mCount;
    private ZDateTime mUntilDate;

    /* NONE: needs no state */
    /* DAILY: need no state */
    /* DAILY_WEEKDAY: needs no state */
    /* DAILY_INTERVAL */
    private int mDailyInterval;
    /* WEEKLY: needs no state */
    /* WEEKLY_BY_DAY */
    private ZWeekDay mWeeklyByDay;
    /* WEEKLY_CUSTOM */
    private int mWeeklyInterval;
    private List<ZWeekDay> mWeeklyIntervalDays;
    /* MONTHLY: needs no state */
    /* MONTHLY_{BY_MONTH_DAY, RELATIVE} */
    private int mMonthlyInterval;
    /* MONTHLY_BY_MONTH_DAY */
    private int mMonthlyMonthDay;
    /* MONTHLY_RELATIVE */
    private ZByDayWeekDay mMonthlyRelativeDay;
    /* YEARLY: needs no state */
    /* YEARLY_BY_DATE */
    private int mYearlyByDateMonthDay;
    private int mYearlyByDateMonth;
    /* YEARLY_RELATIVE */
    private ZByDayWeekDay mYearlyRelativeDay;
    private int mYearlyRelativeMonth;

}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.soap.type.NamedElement;
import com.zimbra.soap.type.ZmBoolean;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Fix Calendar End Times
 * <p>
 * Re-calculate the end times used for calendar items after changes to the recurrence expansion configuration.
 * The current configured values can be determined from:<br />
 * <pre>zmprov getAllConfig | grep zimbraCalendarRecurrence</pre>
 * </p>
 * The default values are:<br />
 * <pre>
 * zimbraCalendarRecurrenceDailyMaxDays: 730
 * zimbraCalendarRecurrenceMaxInstances: 0
 * zimbraCalendarRecurrenceMonthlyMaxMonths: 360
 * zimbraCalendarRecurrenceOtherFrequencyMaxYears: 1
 * zimbraCalendarRecurrenceWeeklyMaxWeeks: 520
 * zimbraCalendarRecurrenceYearlyMaxYears: 100
 * </pre>
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name=AdminConstants.E_FIX_CALENDAR_END_TIME_REQUEST)
public class FixCalendarEndTimeRequest {

    /**
     * @zm-api-field-tag sync
     * @zm-api-field-description Sync flag
     * <table>
     * <tr> <td> <b>1 (true)</b> </td> <td> command blocks until processing finishes </td> </tr>
     * <tr> <td> <b>0 (false) [default]</b> </td> <td> command returns right away </td> </tr>
     * </table>
     */
    @XmlAttribute(name=AdminConstants.A_TZFIXUP_SYNC, required=false)
    private final ZmBoolean sync;

    /**
     * @zm-api-field-description Accounts
     */
    @XmlElement(name=AdminConstants.E_ACCOUNT, required=false)
    private final List<NamedElement> accounts = Lists.newArrayList();

    /**
     * no-argument constructor wanted by JAXB
     */
    @SuppressWarnings("unused")
    private FixCalendarEndTimeRequest() {
        this(null);
    }

    public FixCalendarEndTimeRequest(Boolean sync) {
        this.sync = ZmBoolean.fromBool(sync);
    }

    public void setAccounts(Iterable <NamedElement> accounts) {
        this.accounts.clear();
        if (accounts != null) {
            Iterables.addAll(this.accounts,accounts);
        }
    }

    public FixCalendarEndTimeRequest addAccount(NamedElement account) {
        this.accounts.add(account);
        return this;
    }

    public Boolean getSync() { return ZmBoolean.toBool(sync); }
    public List<NamedElement> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }
}

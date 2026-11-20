// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Map;

/**
 * @author jhahm
 */
public class CalendarResource extends ZAttrCalendarResource {

    public CalendarResource(String name, String id, Map<String, Object> attrs, Map<String, Object> defaults, Provisioning prov) {
        super(name, id, attrs, defaults, prov);
    }
    
    @Override
    public EntryType getEntryType() {
        return EntryType.CALRESOURCE;
    }

	public boolean autoAcceptDecline() {
        return getBooleanAttr(
                Provisioning.A_zimbraCalResAutoAcceptDecline, true);
    }

    public boolean autoDeclineIfBusy() {
        return getBooleanAttr(
                Provisioning.A_zimbraCalResAutoDeclineIfBusy, true);
    }

    public boolean autoDeclineRecurring() {
        return getBooleanAttr(
                Provisioning.A_zimbraCalResAutoDeclineRecurring, false);
    }

    public int getMaxNumConflictsAllowed() {
        return getIntAttr(Provisioning.A_zimbraCalResMaxNumConflictsAllowed, 0);
    }

    public int getMaxPercentConflictsAllowed() {
        return getIntAttr(Provisioning.A_zimbraCalResMaxPercentConflictsAllowed, 0);
    }

}

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

    public String getResourceType() {
        return getAttr(Provisioning.A_zimbraCalResType, "Location");
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

    public String getLocationDisplayName() {
        return getAttr(Provisioning.A_zimbraCalResLocationDisplayName);
    }

    public String getSite() {
        return getAttr(Provisioning.A_zimbraCalResSite);
    }

    public String getBuilding() {
        return getAttr(Provisioning.A_zimbraCalResBuilding);
    }

    public String getFloor() {
        return getAttr(Provisioning.A_zimbraCalResFloor);
    }

    public String getRoom() {
        return getAttr(Provisioning.A_zimbraCalResRoom);
    }

    public int getCapacity() {
        return getIntAttr(Provisioning.A_zimbraCalResCapacity, 0);
    }

    public String getContactName() {
        return getAttr(Provisioning.A_zimbraCalResContactName);
    }

    public String getContactEmail(){
        return getAttr(Provisioning.A_zimbraCalResContactEmail);
    }

    public String getContactPhone(){
        return getAttr(Provisioning.A_zimbraCalResContactPhone);
    }    
    
}

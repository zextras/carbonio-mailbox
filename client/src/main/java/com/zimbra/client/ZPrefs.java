// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import java.util.Collection;
import java.util.Map;
import java.util.TimeZone;

import com.google.common.collect.Iterables;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.calendar.TZIDMapper;

public class ZPrefs {

    private Map<String, Collection<String>> mPrefs;

    public ZPrefs(Map<String, Collection<String>> prefs) {
        mPrefs = prefs;
    }
    
    /**
     * @param name name of pref to get
     * @return null if unset, or first value in list
     */
    public String get(String name) {
        Collection<String> values = mPrefs.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return Iterables.get(values, 0);
    }


    public String getTimeZoneId() { return get(ZAttrProvisioning.A_zimbraPrefTimeZoneId); }

	private TimeZone mCachedTimeZone;
    private String mCachedTimeZoneId;

    public synchronized TimeZone getTimeZone() {
        if (mCachedTimeZone == null || (mCachedTimeZoneId != null && !mCachedTimeZoneId.equals(getTimeZoneId()))) {
            mCachedTimeZoneId = getTimeZoneId();
            mCachedTimeZone  = (mCachedTimeZoneId == null) ? null :
                    TimeZone.getTimeZone(TZIDMapper.canonicalize(mCachedTimeZoneId));
            if (mCachedTimeZone == null)
                mCachedTimeZone = TimeZone.getDefault();
        }
        return mCachedTimeZone;
    }


}

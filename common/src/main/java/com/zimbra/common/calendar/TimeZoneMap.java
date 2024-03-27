// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.calendar;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.util.ZimbraLog;

public class TimeZoneMap implements Cloneable {

    static Map<ZWeekDay, Integer> sDayWeekDayMap;
    static {
        sDayWeekDayMap = new EnumMap<>(ZWeekDay.class);
        sDayWeekDayMap.put(ZWeekDay.SU, java.util.Calendar.SUNDAY);
        sDayWeekDayMap.put(ZWeekDay.MO, java.util.Calendar.MONDAY);
        sDayWeekDayMap.put(ZWeekDay.TU, java.util.Calendar.TUESDAY);
        sDayWeekDayMap.put(ZWeekDay.WE, java.util.Calendar.WEDNESDAY);
        sDayWeekDayMap.put(ZWeekDay.TH, java.util.Calendar.THURSDAY);
        sDayWeekDayMap.put(ZWeekDay.FR, java.util.Calendar.FRIDAY);
        sDayWeekDayMap.put(ZWeekDay.SA, java.util.Calendar.SATURDAY);
    }

    private final Map<String /* real TZID */, ICalTimeZone> mTzMap;
    private final Map<String /* alias */, String /* real TZID */> mAliasMap;
    private final ICalTimeZone mLocalTZ;


    /**
     *
     * @param localTZ local time zone of user account
     */
    public TimeZoneMap(ICalTimeZone localTZ) {
        mTzMap = new HashMap<>();
        mAliasMap = new HashMap<>();
        mLocalTZ = localTZ;
    }

    public Map<String, ICalTimeZone> getMap() {
        return mTzMap;
    }

    public Map<String, String> getAliasMap() {
        return mAliasMap;
    }

    public boolean contains(ICalTimeZone tz) {
        if (tz != null)
            return mTzMap.containsKey(tz.getID());
        else
            return false;
    }

    /**
     *
     * @param localTZ local time zone of user account
     */
    public TimeZoneMap(Map<String, ICalTimeZone> z, Map<String, String> a, ICalTimeZone localTZ) {
        mTzMap = z;
        mAliasMap = a;
        mLocalTZ = localTZ;
    }

    public ICalTimeZone getTimeZone(String tzid) {
        tzid = sanitizeTZID(tzid);
        ICalTimeZone tz = mTzMap.get(tzid);
        if (tz == null) {
            tzid = mAliasMap.get(tzid);
            if (tzid != null)
                tz = mTzMap.get(tzid);
        }
        return tz;
    }

    public ICalTimeZone getLocalTimeZone() {
        return mLocalTZ;
    }

    public Iterator<ICalTimeZone> tzIterator() {
        return mTzMap.values().iterator();
    }

    /**
     * Merge the other timezone map into this one
     *
     * @param other the other timezone map
     */
    public void add(TimeZoneMap other) {
        mAliasMap.putAll(other.mAliasMap);
      for (Entry<String, ICalTimeZone> entry : other.mTzMap.entrySet()) {
        ICalTimeZone zone = entry.getValue();
        if (!mTzMap.containsKey(zone.getID()))
          add(zone);
      }
    }

    public void add(ICalTimeZone tz) {
        String tzid = tz.getID();
        String canonTzid;
        if (!DebugConfig.disableCalendarTZMatchByID) {
            canonTzid = TZIDMapper.canonicalize(tzid);
            ICalTimeZone canonTz = WellKnownTimeZones.getTimeZoneById(canonTzid);
            if (canonTz != null) {
                mTzMap.put(canonTzid, canonTz);
                if (!tzid.equals(canonTzid))
                    mAliasMap.put(tzid, canonTzid);
                return;
            }
        }
        if (!DebugConfig.disableCalendarTZMatchByRule) {
            ICalTimeZone ruleMatch = WellKnownTimeZones.getBestFuzzyMatch(tz);
            if (ruleMatch != null) {
                String realTzid = ruleMatch.getID();
                mTzMap.put(realTzid, ruleMatch);
                if (!tzid.equals(realTzid))
                    mAliasMap.put(tzid, realTzid);
                return;
            }
        }
        mTzMap.put(tzid, tz);
    }

    public static String sanitizeTZID(String tzid) {
        // Workaround for bug in Outlook, which double-quotes TZID parameter
        // value in properties like DTSTART, DTEND, etc. Use unquoted tzId.
        int len = tzid.length();
        if (len >= 2 && tzid.charAt(0) == '"' && tzid.charAt(len - 1) == '"') {
            return tzid.substring(1, len - 1);
        }
        return tzid;
    }

    public ICalTimeZone lookupAndAdd(String tzId) {
        tzId = sanitizeTZID(tzId);
        if ("".equals(tzId))
            return null;

        if (!DebugConfig.disableCalendarTZMatchByID)
            tzId = TZIDMapper.canonicalize(tzId);

        ICalTimeZone zone = getTimeZone(tzId);
        if (zone == null) {
            // Is it a system-defined TZ?
            zone = WellKnownTimeZones.getTimeZoneById(tzId);
            if (zone != null) {
                add(zone);
            } else {
                ZimbraLog.calendar.warn("Encountered time zone with no definition: TZID=%s", tzId);
            }
        }
        return zone;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("{");
        buf.append("LocalTz = ").append(mLocalTZ).append("; others {");
        for (ICalTimeZone i : mTzMap.values()) {
            buf.append(i).append("; ");
        }
        return buf.append("} }").toString();
    }

    // Reduce the timezone map to contain only the TZIDs passed in.
    public void reduceTo(Set<String> tzids) {
        for (Iterator<Map.Entry<String, String>> iter = mAliasMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, String> entry = iter.next();
            String aliasTzid = entry.getKey();
            if (!tzids.contains(aliasTzid))
                iter.remove();
        }
        for (Iterator<Map.Entry<String, ICalTimeZone>> iter = mTzMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, ICalTimeZone> entry = iter.next();
            String id = entry.getKey();
            if (!tzids.contains(id))
                iter.remove();
        }
    }

    @Override
    public TimeZoneMap clone() {
        TimeZoneMap retMap = new TimeZoneMap(mLocalTZ);
        retMap.mTzMap.putAll(mTzMap);
        retMap.mAliasMap.putAll(mAliasMap);
        return retMap;
    }
}

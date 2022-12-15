// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.calendar.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Metadata;

public class CalendarData {
    private int mFolderId;
    private int mModSeq;  // last-modified sequence of the folder
    private long mRangeStart;
    private long mRangeEnd;
    private List<CalendarItemData> mCalendarItems;
    private Map<Integer, CalendarItemData> mCalendarItemsMap;
    private Set<Integer> mStaleItemIds;

    CalendarData(int folderId, int modSeq, long rangeStart, long rangeEnd) {
        mFolderId = folderId;
        mModSeq = modSeq;
        mRangeStart = rangeStart;
        mRangeEnd = rangeEnd;
        mCalendarItems = new ArrayList<CalendarItemData>();
        mCalendarItemsMap = new HashMap<Integer, CalendarItemData>();
        mStaleItemIds = new HashSet<Integer>();
    }

    void addCalendarItem(CalendarItemData calItemData) {
        mCalendarItems.add(calItemData);
        mCalendarItemsMap.put(calItemData.getCalItemId(), calItemData);
    }

    public CalendarItemData getCalendarItemData(int calItemId) {
        return mCalendarItemsMap.get(calItemId);
    }

    public int getFolderId()    { return mFolderId; }
    public int getModSeq()      { return mModSeq; }
    public long getRangeStart() { return mRangeStart; }
    public long getRangeEnd()   { return mRangeEnd; }

    public Iterator<CalendarItemData> calendarItemIterator() { return mCalendarItems.iterator(); }
    public int getNumItems() { return mCalendarItems.size(); }

    public CalendarData getSubRange(long rangeStart, long rangeEnd) {
        if (rangeStart <= mRangeStart && rangeEnd >= mRangeEnd)
            return this;
        CalendarData calData = new CalendarData(mFolderId, mModSeq, rangeStart, rangeEnd);
        for (CalendarItemData calItemData : mCalendarItems) {
            CalendarItemData itemSubRange = calItemData.getSubRange(rangeStart, rangeEnd);
            if (itemSubRange != null) {
                calData.addCalendarItem(itemSubRange);
                int itemId = itemSubRange.getCalItemId();
                if (isItemStale(itemId))
                    calData.markItemStale(itemId);
            }
        }
        return calData;
    }

    synchronized int getNumStaleItems() {
        return mStaleItemIds.size();
    }

    synchronized int markItemStale(int calItemId) {
        mStaleItemIds.add(calItemId);
        return mStaleItemIds.size();
    }

    synchronized boolean isItemStale(int calItemId) {
        return mStaleItemIds.contains(calItemId);
    }

    synchronized void copyStaleItemIdsTo(Set<Integer> copyTo) {
        copyTo.addAll(mStaleItemIds);
    }

    private static final String FN_FOLDER_ID = "fid";
    private static final String FN_MODSEQ = "modSeq";
    private static final String FN_RANGE_START = "rgStart";
    private static final String FN_RANGE_END = "rgEnd";
    private static final String FN_NUM_CALITEMS = "numCi";
    private static final String FN_CALITEM = "ci";

    CalendarData(Metadata meta) throws ServiceException {
        mFolderId = (int) meta.getLong(FN_FOLDER_ID);
        mModSeq = (int) meta.getLong(FN_MODSEQ);
        mRangeStart = meta.getLong(FN_RANGE_START);
        mRangeEnd = meta.getLong(FN_RANGE_END);
        int numCalItems = (int) meta.getLong(FN_NUM_CALITEMS);
        if (numCalItems > 0) {
            mCalendarItems = new ArrayList<CalendarItemData>(numCalItems);
            mCalendarItemsMap = new HashMap<Integer, CalendarItemData>(numCalItems);
            for (int i = 0; i < numCalItems; i++) {
                Metadata metaCalItem = meta.getMap(FN_CALITEM + i, true);
                if (metaCalItem != null) {
                    CalendarItemData calItemData = new CalendarItemData(metaCalItem);
                    addCalendarItem(calItemData);
                }
            }
        } else {
            mCalendarItems = new ArrayList<CalendarItemData>(0);
            mCalendarItemsMap = new HashMap<Integer, CalendarItemData>(0);
        }
        mStaleItemIds = new HashSet<Integer>();
    }

    Metadata encodeMetadata() {
        Metadata meta = new Metadata();
        meta.put(FN_FOLDER_ID, mFolderId);
        meta.put(FN_MODSEQ, mModSeq);
        meta.put(FN_RANGE_START, mRangeStart);
        meta.put(FN_RANGE_END, mRangeEnd);
        meta.put(FN_NUM_CALITEMS, mCalendarItems.size());
        int i = 0;
        for (CalendarItemData calItemData : mCalendarItems) {
            meta.put(FN_CALITEM + i, calItemData.encodeMetadata());
            i++;
        }
        return meta;
    }
}

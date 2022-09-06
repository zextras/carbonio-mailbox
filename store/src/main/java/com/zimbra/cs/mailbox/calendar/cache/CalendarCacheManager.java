// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox.calendar.cache;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.memcached.MemcachedConnector;
import com.zimbra.cs.session.PendingLocalModifications;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CalendarCacheManager {

  // for appointment summary caching (primarily for ZWC)
  private boolean mSummaryCacheEnabled;
  private CalSummaryCache mSummaryCache;

  // for CalDAV ctag caching
  private CalListCache mCalListCache;
  private CtagInfoCache mCtagCache;
  private CtagResponseCache mCtagResponseCache;

  private static CalendarCacheManager sInstance = new CalendarCacheManager();

  public static CalendarCacheManager getInstance() {
    return sInstance;
  }

  private CalendarCacheManager() {
    mCtagCache = new CtagInfoCache();
    mCalListCache = new CalListCache();
    mCtagResponseCache = new CtagResponseCache();

    int summaryLRUSize = 0;
    mSummaryCacheEnabled = LC.calendar_cache_enabled.booleanValue();
    if (mSummaryCacheEnabled) summaryLRUSize = LC.calendar_cache_lru_size.intValue();
    mSummaryCache = new CalSummaryCache(summaryLRUSize);
  }

  public void notifyCommittedChanges(PendingLocalModifications mods, int changeId) {
    if (mSummaryCacheEnabled) mSummaryCache.notifyCommittedChanges(mods, changeId);
    if (MemcachedConnector.isConnected()) {
      mCalListCache.notifyCommittedChanges(mods, changeId);
      mCtagCache.notifyCommittedChanges(mods, changeId);
    }
  }

  public void purgeMailbox(Mailbox mbox) throws ServiceException {
    mSummaryCache.purgeMailbox(mbox);
    if (MemcachedConnector.isConnected()) {
      mCalListCache.purgeMailbox(mbox);
      mCtagCache.purgeMailbox(mbox);
    }
  }

  CtagInfoCache getCtagCache() {
    return mCtagCache;
  }

  public CalSummaryCache getSummaryCache() {
    return mSummaryCache;
  }

  public CtagResponseCache getCtagResponseCache() {
    return mCtagResponseCache;
  }

  public AccountCtags getCtags(AccountKey key) throws ServiceException {
    CalList calList = mCalListCache.get(key);
    if (calList == null) return null;
    Collection<Integer> calendarIds = calList.getCalendars();
    List<CalendarKey> calKeys = new ArrayList<CalendarKey>(calendarIds.size());
    String accountId = key.getAccountId();
    for (int calFolderId : calendarIds) {
      calKeys.add(new CalendarKey(accountId, calFolderId));
    }
    Map<CalendarKey, CtagInfo> ctagsMap = mCtagCache.getMulti(calKeys);
    AccountCtags acctCtags = new AccountCtags(calList, ctagsMap.values());
    return acctCtags;
  }
}

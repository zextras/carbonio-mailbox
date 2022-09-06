// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Oct 6, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.zimbra.cs.account.cache;

import com.zimbra.common.stats.Counter;
import com.zimbra.common.stats.HitRateCounter;
import com.zimbra.common.util.MapUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class AccountCache implements IAccountCache {

  private Map<String, CacheEntry> mNameCache;
  private Map<String, CacheEntry> mIdCache;
  private Map<String, CacheEntry> mAliasCache;
  private Map<String, CacheEntry> mForeignPrincipalCache;
  private Map<String, CacheEntry> mOldNameCache;
  private Counter mHitRate = new HitRateCounter();

  private long mRefreshTTL;

  static class CacheEntry {
    long mLifetime;
    Account mEntry;

    CacheEntry(Account entry, long expires) {
      mEntry = entry;
      mLifetime = System.currentTimeMillis() + expires;
    }

    boolean isStale() {
      return mLifetime < System.currentTimeMillis();
    }
  }

  /**
   * @param maxItems
   * @param refreshTTL
   */
  public AccountCache(int maxItems, long refreshTTL) {
    mNameCache = MapUtil.newLruMap(maxItems);
    mIdCache = MapUtil.newLruMap(maxItems);
    mAliasCache = MapUtil.newLruMap(maxItems);
    mForeignPrincipalCache = MapUtil.newLruMap(maxItems);
    mOldNameCache = MapUtil.newLruMap(maxItems);
    mRefreshTTL = refreshTTL;
  }

  @Override
  public synchronized void clear() {
    mNameCache.clear();
    mIdCache.clear();
    mAliasCache.clear();
    mForeignPrincipalCache.clear();
    mOldNameCache.clear();
  }

  @Override
  public synchronized void remove(Account entry) {
    if (entry != null) {
      mNameCache.remove(entry.getName());
      mIdCache.remove(entry.getId());

      String aliases[] = entry.getMultiAttr(Provisioning.A_zimbraMailAlias);
      for (String alias : aliases) {
        mAliasCache.remove(alias);
      }

      String fps[] = entry.getMultiAttr(Provisioning.A_zimbraForeignPrincipal);
      for (String fp : fps) {
        mForeignPrincipalCache.remove(fp);
      }
      mOldNameCache.remove(entry.getOldMailAddress());
    }
  }

  @Override
  public synchronized void put(Account entry) {
    if (entry != null) {
      CacheEntry cacheEntry = new CacheEntry(entry, mRefreshTTL);
      mNameCache.put(entry.getName(), cacheEntry);
      mIdCache.put(entry.getId(), cacheEntry);

      String aliases[] = entry.getMultiAttr(Provisioning.A_zimbraMailAlias);
      for (String alias : aliases) {
        mAliasCache.put(alias, cacheEntry);
      }

      String fps[] = entry.getMultiAttr(Provisioning.A_zimbraForeignPrincipal);
      for (String fp : fps) {
        mForeignPrincipalCache.put(fp, cacheEntry);
      }
      if (StringUtils.isNotEmpty(entry.getOldMailAddress())) {
        mOldNameCache.put(entry.getOldMailAddress(), cacheEntry);
      }
    }
  }

  @Override
  public synchronized void replace(Account entry) {
    remove(entry);
    put(entry);
  }

  private Account get(String key, Map cache) {
    CacheEntry ce = (CacheEntry) cache.get(key);
    if (ce != null) {
      if (mRefreshTTL != 0 && ce.isStale()) {
        remove(ce.mEntry);
        mHitRate.increment(0);
        return null;
      } else {
        mHitRate.increment(100);
        return ce.mEntry;
      }
    } else {
      mHitRate.increment(0);
      return null;
    }
  }

  @Override
  public synchronized Account getById(String key) {
    return get(key, mIdCache);
  }

  @Override
  public synchronized Account getByName(String key) {
    Account acct =
        get(key.toLowerCase(), mNameCache) != null
            ? get(key.toLowerCase(), mNameCache)
            : get(key.toLowerCase(), mAliasCache) != null
                ? get(key.toLowerCase(), mAliasCache)
                : get(key.toLowerCase(), mOldNameCache);
    return acct;
  }

  @Override
  public synchronized Account getByForeignPrincipal(String key) {
    return get(key, mForeignPrincipalCache);
  }

  @Override
  public synchronized int getSize() {
    return mIdCache.size();
  }

  /** Returns the cache hit rate as a value between 0 and 100. */
  @Override
  public synchronized double getHitRate() {
    return mHitRate.getAverage();
  }
}

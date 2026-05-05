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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zimbra.common.stats.Counter;
import com.zimbra.common.stats.HitRateCounter;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;

public class AccountCache implements IAccountCache {

    private final Cache<String, Account> mNameCache;
    private final Cache<String, Account> mIdCache;
    private final Cache<String, Account> mAliasCache;
    private final Cache<String, Account> mForeignPrincipalCache;
    private final Cache<String, Account> mOldNameCache;
    private final Counter mHitRate = new HitRateCounter();

    /**
     * @param maxItems
     * @param refreshTTL
     */
    public AccountCache(int maxItems, long refreshTTL) {
        mNameCache = buildCache(maxItems, refreshTTL);
        mIdCache = buildCache(maxItems, refreshTTL);
        mAliasCache = buildCache(maxItems, refreshTTL);
        mForeignPrincipalCache = buildCache(maxItems, refreshTTL);
        mOldNameCache = buildCache(maxItems, refreshTTL);
    }

    private Cache<String, Account> buildCache(int maxItems, long refreshTTL) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder().maximumSize(maxItems);
        if (refreshTTL > 0) {
            builder = builder.expireAfterWrite(refreshTTL, TimeUnit.MILLISECONDS);
        }
        return builder.build();
    }

    @Override
    public void clear() {
        mNameCache.invalidateAll();
        mIdCache.invalidateAll();
        mAliasCache.invalidateAll();
        mForeignPrincipalCache.invalidateAll();
        mOldNameCache.invalidateAll();
    }

    @Override
    public void remove(Account entry) {
        if (entry != null) {
            mNameCache.invalidate(entry.getName());
            mIdCache.invalidate(entry.getId());

            String[] aliases = entry.getMultiAttr(Provisioning.A_zimbraMailAlias);
            for (String alias : aliases) {
                mAliasCache.invalidate(alias);
            }

            String[] fps = entry.getMultiAttr(Provisioning.A_zimbraForeignPrincipal);
            for (String fp : fps) {
                mForeignPrincipalCache.invalidate(fp);
            }
            mOldNameCache.invalidate(entry.getOldMailAddress());
        }
    }

    @Override
    public void put(Account entry) {
        if (entry != null) {
            mNameCache.put(entry.getName(), entry);
            mIdCache.put(entry.getId(), entry);

            String[] aliases = entry.getMultiAttr(Provisioning.A_zimbraMailAlias);
            for (String alias : aliases) {
                mAliasCache.put(alias, entry);
            }

            String[] fps = entry.getMultiAttr(Provisioning.A_zimbraForeignPrincipal);
            for (String fp : fps) {
                mForeignPrincipalCache.put(fp, entry);
            }
            if (StringUtils.isNotEmpty(entry.getOldMailAddress())) {
                mOldNameCache.put(entry.getOldMailAddress(), entry);
            }
        }
    }

    @Override
    public void replace(Account entry) {
        remove(entry);
        put(entry);
    }

    private Account get(String key, Cache<String, Account> cache) {
        Account acct = cache.getIfPresent(key);
        mHitRate.increment(acct != null ? 100 : 0);
        return acct;
    }

    @Override
    public Account getById(String key) {
        return get(key, mIdCache);
    }

    @Override
    public Account getByName(String key) {
        String lk = key.toLowerCase();
        Account acct = mNameCache.getIfPresent(lk);
        if (acct == null) acct = mAliasCache.getIfPresent(lk);
        if (acct == null) acct = mOldNameCache.getIfPresent(lk);
        mHitRate.increment(acct != null ? 100 : 0);
        return acct;
    }

    @Override
    public Account getByForeignPrincipal(String key) {
        return get(key, mForeignPrincipalCache);
    }

    @Override
    public int getSize() {
        return (int) mIdCache.estimatedSize();
    }

    /**
     * Returns the cache hit rate as a value between 0 and 100.
     */
    @Override
    public double getHitRate() {
        return mHitRate.getAverage();
    }
}

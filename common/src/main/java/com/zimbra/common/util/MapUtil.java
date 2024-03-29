// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;

public class MapUtil {

    public static <K, V> TimeoutMap<K, V> newTimeoutMap(long timeoutMillis) {
        return new TimeoutMap<>(timeoutMillis);
    }
    
    public static <K, V> LruMap<K, V> newLruMap(int maxSize) {
        return new LruMap<>(maxSize);
    }

    /**
     * Returns a new {@code LoadingCache} that maps a key to a {@code List} of values.
     * When {@code get()} is called on a key that does not exist in the map,
     * the map implicitly creates a new key that maps to an empty {@code List}. 
     */
    public static <K, V> LoadingCache<K, List<V>> newValueListMap() {
        Function<K, List<V>> listCreator = from -> new ArrayList<>();
        LoadingCache<K, List<V>> cache = CacheBuilder.newBuilder()
            .build(CacheLoader.from(listCreator));
        return cache;
    }
    
    /**
     * Returns a new {@code LoadingCache} that maps a key to a {@code Set} of values.
     * When {@code get()} is called on a key that does not exist in the map,
     * the map implicitly creates a new key that maps to an empty {@code Set}. 
     */
    public static <K, V> LoadingCache<K, Set<V>> newValueSetMap() {
        Function<K, Set<V>> setCreator = from -> new HashSet<>();
        LoadingCache<K, Set<V>> cache = CacheBuilder.newBuilder()
            .build(CacheLoader.from(setCreator));
        return cache;
    }
    
    /**
     * Converts a Guava {@code Multimap} to a {@code Map} that maps a key to a
     * {@code List} of values.
     */
    public static <K, V> Map<K, List<V>> multimapToMapOfLists(Multimap<K, V> multimap) {
        LoadingCache<K, List<V>> loadingCache = newValueListMap();
        if (multimap != null) {
            for (Map.Entry<K, V> entry : multimap.entries()) {
                LoadingCacheUtil.get(loadingCache, entry.getKey()).add(entry.getValue());
            }
        }
        return LoadingCacheUtil.getAll(loadingCache, multimap.keySet());
    }
}

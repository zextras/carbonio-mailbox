// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class MapUtilTest {

  @Test
  void newValueListMap() throws ExecutionException {
    LoadingCache<Integer, List<String>> map = MapUtil.newValueListMap();
    List<String> list1 = new ArrayList<String>();
    list1.add("a");
    list1.add("b");
    List<String> list2 = new ArrayList<String>();
    list2.add("c");
    map.put(1, list1);
    map.put(2, list2);

    assertEquals(2, map.size());

    List<String> list = map.get(1);
    assertEquals(2, list.size());
    assertTrue(list.contains("a"));
    assertTrue(list.contains("b"));

    list = map.get(2);
    assertEquals(1, list.size());
    assertTrue(list.contains("c"));
  }

  @Test
  void newValueListMapCreatesEmptyListForUnknownKey() throws ExecutionException {
    LoadingCache<String, List<String>> map = MapUtil.newValueListMap();
    map.get("missing").add("value");
    assertEquals(List.of("value"), map.get("missing"));
  }

  @Test
  void multimapToMapOfLists() {
    Multimap<String, Integer> multimap = ArrayListMultimap.create();
    multimap.put("a", 1);
    multimap.put("a", 2);
    multimap.put("b", 3);

    Map<String, List<Integer>> map = MapUtil.multimapToMapOfLists(multimap);

    assertEquals(Map.of("a", List.of(1, 2), "b", List.of(3)), map);
  }

  @Test
  void newLruMapEvictsLeastRecentlyUsed() {
    LruMap<String, Integer> map = MapUtil.newLruMap(2);
    map.put("a", 1);
    map.put("b", 2);
    map.get("a");
    map.put("c", 3);

    assertEquals(2, map.getMaxSize());
    assertEquals(Map.of("a", 1, "c", 3), map);
  }

  @Test
  void newTimeoutMapStoresValues() {
    TimeoutMap<String, Integer> map = MapUtil.newTimeoutMap(60_000);
    map.put("a", 1);
    assertEquals(1, map.get("a"));
    assertFalse(map.isEmpty());
  }
}

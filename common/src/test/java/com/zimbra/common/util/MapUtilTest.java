// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.LoadingCache;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
  void newValueSetMap() throws ExecutionException {
    LoadingCache<Integer, Set<String>> map = MapUtil.newValueSetMap();
    Set<String> set1 = new HashSet<String>();
    set1.add("a");
    set1.add("b");
    Set<String> set2 = new HashSet<String>();
    set2.add("c");
    map.put(1, set1);
    map.put(2, set2);

    assertEquals(2, map.size());

    Set<String> set = map.get(1);
    assertEquals(2, set.size());
    assertTrue(set.contains("a"));
    assertTrue(set.contains("b"));

    set = map.get(2);
    assertEquals(1, set.size());
    assertTrue(set.contains("c"));
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class ListUtilTest {

  @Test
  void newArrayList() {
    Function<Integer, String> intToString = new Function<Integer, String>() {
      public String apply(Integer i) {
        return i.toString();
      }
    };

    List<Integer> intList = Lists.newArrayList(1, 2);
    List<String> stringList = ListUtil.newArrayList(intList, intToString);

    // Check transformed list.
    assertEquals(2, stringList.size());
    assertEquals("1", stringList.get(0));
    assertEquals("2", stringList.get(1));

    // Make changes to the transformed list and make sure the original list isn't affected.
    stringList.remove(0);
    stringList.set(0, "3");
    assertEquals(2, intList.size());
    assertEquals(1, (int) intList.get(0));
    assertEquals(2, (int) intList.get(1));
  }

  @Test
  void newArrayListFiltersWithPredicate() {
    Predicate<Integer> isEven = i -> i % 2 == 0;
    List<Integer> evens = ListUtil.newArrayList(List.of(1, 2, 3, 4), isEven);
    assertEquals(List.of(2, 4), evens);
  }

  @Test
  void nullToEmpty() {
    Collection<Integer> c = null;
    assertEquals(0, ListUtil.nullToEmpty(c).size());

    List<Integer> l = Lists.newArrayList(1, 2, 3);
    assertEquals(l, ListUtil.nullToEmpty(l));
  }

  @Test
  void isEmpty() {
    assertTrue(ListUtil.isEmpty(null));
    assertTrue(ListUtil.isEmpty(List.of()));
    assertFalse(ListUtil.isEmpty(List.of(1)));
  }

  @Test
  void getFirstElement() {
    assertNull(ListUtil.getFirstElement(null));
    assertNull(ListUtil.getFirstElement(List.of()));
    assertEquals("a", ListUtil.getFirstElement(List.of("a", "b")));
  }

  @Test
  void mergeSortedListsKeepsDuplicates() {
    List<Integer> merged = merge(false, List.of(1, 3, 5), List.of(1, 2, 6), List.of(4));
    assertEquals(List.of(1, 1, 2, 3, 4, 5, 6), merged);
  }

  @Test
  void mergeSortedListsRemovesDuplicates() {
    List<Integer> merged = merge(true, List.of(1, 3, 3, 5), List.of(1, 3, 6));
    assertEquals(List.of(1, 3, 5, 6), merged);
  }

  @Test
  void mergeSortedListsSkipsEmptySources() {
    List<Integer> merged = merge(false, List.of(), List.of(2, 4), List.of(), List.of(1));
    assertEquals(List.of(1, 2, 4), merged);
  }

  @Test
  void mergeSortedListsCopiesSingleNonNullSource() {
    List<Integer> merged = merge(false, null, List.of(3, 1), null);
    assertEquals(List.of(3, 1), merged);
  }

  @Test
  void mergeSortedListsWithNoSources() {
    assertEquals(List.of(), merge(true));
  }

  @Test
  void subtractSortedLists() {
    Comparator<Integer> natural = Comparator.naturalOrder();
    assertEquals(List.of(2, 4), ListUtil.subtractSortedLists(List.of(1, 2, 3, 4, 5), List.of(1, 3, 5, 7), natural));
    assertEquals(List.of(3, 5, 9), ListUtil.subtractSortedLists(List.of(1, 3, 5, 7, 9), List.of(1, 7, 12), natural));
    assertEquals(List.of(1, 2), ListUtil.subtractSortedLists(List.of(1, 2), List.of(), natural));
    assertEquals(List.of(), ListUtil.subtractSortedLists(List.of(), List.of(1), natural));
    assertEquals(List.of(5), ListUtil.subtractSortedLists(List.of(5), List.of(1), natural));
  }

  @Test
  void splitIntoChunks() {
    assertEquals(
        List.of(List.of(1, 2), List.of(3, 4), List.of(5)), ListUtil.split(List.of(1, 2, 3, 4, 5), 2));
    assertEquals(List.of(List.of(1, 2)), ListUtil.split(List.of(1, 2), 2));
  }

  @Test
  void splitNullAndEmpty() {
    assertNull(ListUtil.split(null, 2));
    assertEquals(List.of(), ListUtil.split(Set.of(), 2));
  }

  @Test
  void mainRunsSelfTest() {
    assertDoesNotThrow(() -> ListUtil.main(new String[0]));
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  private static List<Integer> merge(boolean removeDuplicates, List<Integer>... sources) {
    List<Integer> dest = new ArrayList<>();
    ListUtil.mergeSortedLists(dest, sources, removeDuplicates);
    return dest;
  }
}

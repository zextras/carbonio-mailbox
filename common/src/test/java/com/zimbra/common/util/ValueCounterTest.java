// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ValueCounterTest {

  @Test
  void countsIncrementsAndDecrements() {
    ValueCounter<String> counter = new ValueCounter<>();
    counter.increment("a");
    counter.increment("a");
    counter.increment("b", 5);
    counter.decrement("b");

    assertEquals(2, counter.getCount("a"));
    assertEquals(4, counter.getCount("b"));
    assertEquals(0, counter.getCount("missing"));
    assertEquals(2, counter.size());
    assertEquals(6, counter.getTotal());
  }

  @Test
  void decrementOfUnknownValueGoesNegative() {
    ValueCounter<String> counter = new ValueCounter<>();
    counter.decrement("a");
    assertEquals(-1, counter.getCount("a"));
  }

  @Test
  void iteratesOverDistinctValues() {
    ValueCounter<String> counter = counterOf("a", "b", "a");
    Set<String> values = new HashSet<>();
    counter.iterator().forEachRemaining(values::add);
    assertEquals(Set.of("a", "b"), values);
  }

  @Test
  void clear() {
    ValueCounter<String> counter = counterOf("a");
    counter.clear();
    assertEquals(0, counter.size());
    assertEquals(0, counter.getTotal());
  }

  @Test
  void toStringListsCounts() {
    assertEquals("a: 2", counterOf("a", "a").toString());
    String twoValues = counterOf("a", "b").toString();
    assertTrue(twoValues.equals("a: 1, b: 1") || twoValues.equals("b: 1, a: 1"));
    assertEquals("", new ValueCounter<String>().toString());
  }

  private static ValueCounter<String> counterOf(String... values) {
    ValueCounter<String> counter = new ValueCounter<>();
    for (String value : values) {
      counter.increment(value);
    }
    return counter;
  }
}

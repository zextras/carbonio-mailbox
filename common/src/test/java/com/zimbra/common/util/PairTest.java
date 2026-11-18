// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class PairTest {

  @Test
  void accessors() {
    Pair<String, Integer> pair = new Pair<>("a", 1);
    assertEquals("a", pair.getFirst());
    assertEquals("a", pair.car());
    assertEquals(1, pair.getSecond());
  }

  @Test
  void setters() {
    Pair<String, Integer> pair = new Pair<>("a", 1);
    pair.setFirst("b");
    pair.setSecond(2);
    assertEquals(new Pair<>("b", 2), pair);
  }

  @Test
  void equalsComparesBothElements() {
    Pair<String, String> pair = new Pair<>("a", "b");
    assertEquals(pair, pair);
    assertEquals(new Pair<>("a", "b"), new Pair<>(new String("a"), new String("b")));
    assertEquals(new Pair<String, String>(null, null), new Pair<String, String>(null, null));
    assertNotEquals(new Pair<>("a", "b"), new Pair<>("x", "b"));
    assertNotEquals(new Pair<>(null, "b"), new Pair<>("a", "b"));
    assertNotEquals(new Pair<>("a", "b"), new Pair<>("a", "x"));
    assertNotEquals(new Pair<>("a", null), new Pair<>("a", "b"));
  }

  @Test
  void notEqualToOtherTypes() {
    assertNotEquals(new Pair<>("a", "b"), "(a,b)");
  }

  @Test
  void hashCodeCombinesElements() {
    assertEquals("a".hashCode() ^ "b".hashCode(), new Pair<>("a", "b").hashCode());
    assertEquals(0, new Pair<>(null, null).hashCode());
  }

  @Test
  void toStringShowsBothElements() {
    assertEquals("(a,null)", new Pair<>("a", null).toString());
  }

  @Test
  void mainPrintsSamples() {
    assertDoesNotThrow(() -> Pair.main(new String[0]));
  }
}

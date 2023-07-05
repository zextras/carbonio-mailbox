// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.base.Function;
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
  void nullToEmpty() {
    Collection<Integer> c = null;
    assertEquals(0, ListUtil.nullToEmpty(c).size());

    List<Integer> l = Lists.newArrayList(1, 2, 3);
    assertEquals(l, ListUtil.nullToEmpty(l));
  }
}

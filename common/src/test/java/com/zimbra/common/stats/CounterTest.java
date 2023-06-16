// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;

public class CounterTest {

  @Test
  void testIncrement() {
    Counter counter = new Counter();
    assertEquals(0, counter.getAverage());

    counter.increment();
    assertEquals(1, counter.getAverage()); //1 hit

    counter.increment();
    assertEquals(1, counter.getAverage()); //2 hits

    counter.increment(0);
    counter.increment(0);
    assertEquals(0.5, counter.getAverage()); //2 hits, 2 misses

    counter.increment(0);
    counter.increment(0);
    counter.increment(0);
    counter.increment(0);
    assertEquals(0.25, counter.getAverage()); //2 hits, 6 misses

    counter.increment();
    counter.increment();
    assertEquals(0.4, counter.getAverage()); //4 hits, 6 misses

    counter.reset();
    assertEquals(0, counter.getAverage());
  }
}

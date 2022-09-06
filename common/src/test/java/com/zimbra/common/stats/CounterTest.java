// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.stats;

import org.junit.Assert;
import org.junit.Test;

public class CounterTest {

  @Test
  public void testIncrement() {
    Counter counter = new Counter();
    Assert.assertTrue(0 == counter.getAverage());

    counter.increment();
    Assert.assertTrue(1 == counter.getAverage()); // 1 hit

    counter.increment();
    Assert.assertTrue(1 == counter.getAverage()); // 2 hits

    counter.increment(0);
    counter.increment(0);
    Assert.assertTrue(0.5 == counter.getAverage()); // 2 hits, 2 misses

    counter.increment(0);
    counter.increment(0);
    counter.increment(0);
    counter.increment(0);
    Assert.assertTrue(0.25 == counter.getAverage()); // 2 hits, 6 misses

    counter.increment();
    counter.increment();
    Assert.assertTrue(0.4 == counter.getAverage()); // 4 hits, 6 misses

    counter.reset();
    Assert.assertTrue(0 == counter.getAverage());
  }
}

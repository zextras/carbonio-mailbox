// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.stats;

import java.util.concurrent.atomic.AtomicLong;

/** Tracks a total and count (number of calls to {@link #increment}). */
public class Counter {

  private final AtomicLong mCount = new AtomicLong();
  private final AtomicLong mTotal = new AtomicLong();
  /** Counter name */
  protected final String name;

  public String getName() {
    return name;
  }

  public Counter(String name) {
    this.name = name;
  }

  public long getCount() {
    return mCount.longValue();
  }

  public long getTotal() {
    return mTotal.longValue();
  }

  /** Returns the average since the last call to {@link #reset}. */
  public synchronized double getAverage() {
    if (mCount.longValue() == 0) {
      return 0.0;
    } else {
      return (double) mTotal.longValue() / (double) mCount.longValue();
    }
  }

  /** Increments the total by the specified value. Increments the count by 1. */
  public void increment(long value) {
    mCount.getAndIncrement();
    mTotal.getAndAdd(value);
  }

  /** Increments the count and total by 1. */
  public void increment() {
    increment(1);
  }

  public synchronized void reset() {
    mCount.set(0);
    mTotal.set(0);
  }
}

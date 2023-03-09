// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.stats;

import com.zimbra.common.stats.Counter;
import com.zimbra.common.stats.StatsDumperDataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ActivityTrackers get their own output file (e.g. soap.csv) and track a set of "commands" and
 * their "total elapsed time" for each counter stat period, one on a line.
 */
public class ActivityTracker implements StatsDumperDataSource {

  private final String mFilename;
  private final ConcurrentHashMap<String, Counter> mCounterMap = new ConcurrentHashMap<>();

  public ActivityTracker(String filename) {
    mFilename = filename;
  }

  public void addStat(String commandName, long startTime) {
    Counter counter = getCounter(commandName);
    counter.increment(System.currentTimeMillis() - startTime);
  }

  public Counter getCounter(String commandName) {
    Counter counter = mCounterMap.get(commandName);
    if (counter == null) {
      counter = new Counter();

      Counter previousCounter = mCounterMap.putIfAbsent(commandName, counter);
      if (previousCounter != null) {
        // Another thread added the counter after the get() check.  Use it instead
        // of the one we just instantiated.
        counter = previousCounter;
      }
    }
    return counter;
  }

  /**
   * Returns a map of all counter names and values for counter values greater than zero.
   *
   * @return all registered counters
   */
  public Map<String, Counter> getCounters() {
    return mCounterMap.entrySet().stream()
        .filter(entry -> entry.getValue().getCount() > 0)
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  ////////////// StatsDumperDataSource implementation //////////////
  public Collection<String> getDataLines() {
    List<String> dataLines = new ArrayList<>(getCounters().size());
    getCounters()
        .forEach(
            (command, counter) -> {
              // This code is not thread-safe, but should be good enough 99.9% of the time.
              // We avoid synchronization at the risk of the numbers being slightly off
              // during a race condition.
              long count = counter.getCount();
              long avg = (long) counter.getAverage();
              counter.reset();
              dataLines.add(String.format("%s,%d,%d", command, count, avg));
            });
    return dataLines;
  }

  public String getFilename() {
    return mFilename;
  }

  public String getHeader() {
    return "command,exec_count,exec_ms_avg";
  }

  public boolean hasTimestampColumn() {
    return true;
  }
}

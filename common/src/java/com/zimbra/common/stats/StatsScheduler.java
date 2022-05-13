package com.zimbra.common.stats;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StatsScheduler {
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
  /**
   * Schedules a new stats task.
   *
   * @param dumper the data source
   * @param intervalMillis interval between writes. The first write is delayed by this interval.
   */
  public void schedule(Dumper dumper, long intervalMillis) {
    final StatsDumperTask statsDumperTask = new StatsDumperTask(dumper);
    executor.scheduleAtFixedRate(statsDumperTask, 0, intervalMillis, TimeUnit.MILLISECONDS);
  }
}

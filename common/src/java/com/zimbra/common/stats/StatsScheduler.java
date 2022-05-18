package com.zimbra.common.stats;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * StatsScheduler class, used for scheduling StatsDumpers {@link Dumper} tasks are executed using
 * {@link ScheduledExecutorService} ({@link #scheduledExecutorService})
 *
 * @author Keshav Bhatt
 * @since 4.0.7
 */
public class StatsScheduler {
  private static final Lock singletonCreation = new ReentrantLock();
  private final ScheduledExecutorService scheduledExecutorService;
  private static StatsScheduler singleton = null;

  private StatsScheduler(ScheduledExecutorService scheduledExecutorService) {
    this.scheduledExecutorService = scheduledExecutorService;
  }
  /**
   * @return the default initialization for this object. Note the following invocations of this
   *     method will return the same {@link StatsScheduler}
   */
  public static StatsScheduler getDefault() {
    if (singleton == null) {
      try {
        singletonCreation.lock();
        // Double check intended: the first one allows us to skip locking if not necessary, the
        // second one actually checks the object instance is not there yet in a thread-safe way.
        if (singleton == null) {
          // Note: do not bump the pool size to too much, these threads gets reserved for the
          // executor until shutdown or interrupt happens.
          singleton = new StatsScheduler(Executors.newScheduledThreadPool(6));
        }
      } finally {
        singletonCreation.unlock();
      }
    }
    return singleton;
  }
  /**
   * Schedules a new stats task.
   *
   * @param dumper the data source
   * @param intervalMillis interval between writes. The first write is delayed by this interval.
   */
  public void schedule(Dumper dumper, long intervalMillis) {
    final StatsDumperTask statsDumperTask = new StatsDumperTask(dumper);
    scheduledExecutorService.scheduleAtFixedRate(
        statsDumperTask, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
  }
}

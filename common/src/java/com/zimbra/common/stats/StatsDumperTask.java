package com.zimbra.common.stats;

import com.zimbra.common.util.ZimbraLog;
import java.util.concurrent.Callable;

/**
 * StatsDumperTask class
 * @author Keshav Bhatt
 * @since 4.0.7
 * */
public class StatsDumperTask implements Runnable {
  private final long intervalMillis;
  private final StatsDumperDataSource dataSource;
  private final Callable<Void> dumper;

  /**
   * @param intervalMillis interval in milli-seconds this thread will wait to call the dumper
   * @param dataSource {@link StatsDumperDataSource} data source for which the task is concerned with
   * @param dumper dumper {@link Callable} instance which the task is concerned with
   */
  public StatsDumperTask(
      long intervalMillis, StatsDumperDataSource dataSource, Callable<Void> dumper) {
    this.intervalMillis = intervalMillis;
    this.dataSource = dataSource;
    this.dumper = dumper;
  }

  @Override
  public void run() {
    //noinspection InfiniteLoopStatement
    while (true) {
      try {
        //noinspection BusyWait
        Thread.sleep(intervalMillis);
        try {
          dumper.call();
        } catch (Exception e) {
          ZimbraLog.perf.warn("Exception in stats thread: %s", dataSource.getFilename(), e);
        }
      } catch (InterruptedException e) {
        ZimbraLog.perf.info("Stats thread interrupted: %s", dataSource.getFilename(), e);
      }
      if (Thread.currentThread().isInterrupted()) {
        ZimbraLog.perf.info("Stats thread was interrupted: %s", dataSource.getFilename());
      }
    }
  }
}

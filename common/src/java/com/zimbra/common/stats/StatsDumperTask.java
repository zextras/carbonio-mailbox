package com.zimbra.common.stats;

import com.zimbra.common.util.ZimbraLog;
import java.util.concurrent.Callable;

/**
 * StatsDumperTask class
 *
 * @author Keshav Bhatt
 * @since 4.0.7
 */
public class StatsDumperTask implements Runnable {

  private final Dumper dumper;

  /**
   * Construct {@link StatsDumperTask} object
   *
   * @param dumper dumper {@link Callable} instance which the task is concerned with
   */
  public StatsDumperTask(Dumper dumper) {
    this.dumper = dumper;
  }

  @Override
  public void run() {
    try {
      dumper.call();
    } catch (Exception e) {
      ZimbraLog.perf.warn("Exception in stats thread: %s", dumper.getDataSource().getFilename(), e);
    }
  }
}

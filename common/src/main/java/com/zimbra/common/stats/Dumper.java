package com.zimbra.common.stats;

import java.util.concurrent.Callable;

public interface Dumper extends Callable<Void> {

  /**
   * Return DataSource defined for the dumper
   *
   * @return {@link StatsDumperDataSource} Dumper's DataSource
   */
  StatsDumperDataSource getDataSource();
}

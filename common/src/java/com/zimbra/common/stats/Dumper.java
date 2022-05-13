package com.zimbra.common.stats;

import java.util.concurrent.Callable;

public interface Dumper extends Callable<Void> {
  StatsDumperDataSource getDataSource();
}

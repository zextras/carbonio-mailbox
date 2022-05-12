package com.zimbra.common.stats;

import com.zimbra.common.util.FileUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Writes data to a file at a scheduled interval. Data, headers and filename are retrieved from a
 * {@link StatsDumperDataSource}.
 *
 * @author Keshav Bhatt
 * @since 4.0.7
 */
public class StatsDumperPrometheus implements Callable<Void> {

  private static final File STATS_DIR = new File("/opt/zextras/zmstat/prometheus");
  private static final ThreadGroup STATS_GROUP = new ThreadGroup("ZimbraPerf Prometheus Stats");
  private final StatsDumperDataSource mDataSource;

  private StatsDumperPrometheus(StatsDumperDataSource dataSource) {
    mDataSource = dataSource;
  }

  /**
   * Schedules a new stats task in a thread & calls the dumper to write collected data to the stats
   * file
   *
   * @param dataSource the data source {@link StatsDumperDataSource}
   * @param intervalMillis interval between writes. The first write is delayed by this interval.
   */
  public static void schedule(final StatsDumperDataSource dataSource, final long intervalMillis) {
    final StatsDumperPrometheus dumper = new StatsDumperPrometheus(dataSource);
    final StatsDumperTask r = new StatsDumperTask(intervalMillis, dataSource, dumper);
    new Thread(STATS_GROUP, r, dataSource.getFilename()).start();
  }

  /**
   * Ensures that the directory to hold stat exists and return the file object for the stat file we
   * want to write metrics to
   *
   * @return file object {@link File} for current dataSource
   * @throws IOException IO exception that could occur while creating the file object
   */
  private File getFile() throws IOException {
    FileUtil.ensureDirExists(STATS_DIR);
    return new File(STATS_DIR, mDataSource.getFilename());
  }

  /**
   * Gets the latest data from the dataSource and writes it to the file
   *
   * @throws Exception if any exception occurs
   */
  public Void call() throws Exception {

    StringBuilder logBuffer = PrometheusFormatter.format(mDataSource);
    if (logBuffer != null) {
      writeLogBuffer(logBuffer);
    }
    return null;
  }

  /**
   * Writes log buffer to metrics file
   *
   * @param logBuffer the logBuffer containing processed data
   * @throws IOException exception that could occur while writing data to file
   */
  private void writeLogBuffer(final StringBuilder logBuffer) throws IOException {
    File file = getFile();
    try (FileWriter writer = new FileWriter(file, false)) {
      writer.write(logBuffer.toString());
    }
  }
}

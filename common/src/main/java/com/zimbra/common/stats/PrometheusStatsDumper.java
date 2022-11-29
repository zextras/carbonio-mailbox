package com.zimbra.common.stats;

import com.zimbra.common.util.FileUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Writes data to a file at a scheduled interval. Data, headers and filename are retrieved from a
 * {@link StatsDumperDataSource}.
 *
 * @author Keshav Bhatt
 * @since 4.0.7
 */
public class PrometheusStatsDumper implements Dumper {

  private static final File STATS_DIR = new File("/opt/zextras/zmstat/prometheus");
  private static final String STAT_FILE_EXTENSION = ".prom";
  private final StatsDumperDataSource mDataSource;

  public PrometheusStatsDumper(StatsDumperDataSource dataSource) {
    mDataSource = dataSource;
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
    final File statsFile = new File(STATS_DIR, mDataSource.getFilename() + STAT_FILE_EXTENSION);
    if (statsFile.createNewFile()) {
      //noinspection ResultOfMethodCallIgnored
      statsFile.setReadable(true, false);
    }
    return statsFile;
  }

  /**
   * Gets the latest data from the dataSource and writes it to the file
   *
   * @throws Exception if any exception occurs
   */
  public Void call() throws Exception {
    final StringBuilder logBuffer = PrometheusFormatter.format(mDataSource);
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
    final File file = getFile();
    try (FileWriter writer = new FileWriter(file, false)) {
      writer.write(logBuffer.toString());
    }
  }

  @Override
  public StatsDumperDataSource getDataSource() {
    return mDataSource;
  }
}

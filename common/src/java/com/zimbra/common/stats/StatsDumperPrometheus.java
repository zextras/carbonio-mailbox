package com.zimbra.common.stats;

import com.zimbra.common.util.FileUtil;
import com.zimbra.common.util.ZimbraLog;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Writes data to a file at a scheduled interval. Data and headers are retrieved from a {@link
 * StatsDumperDataSource}.
 */
public class StatsDumperPrometheus implements Callable<Void> {

  private static final File STATS_DIR = new File("/opt/zextras/zmstat/prometheus");
  private static final ThreadGroup statsGroup = new ThreadGroup("ZimbraPerf Prometheus Stats");

  private final StatsDumperDataSource mDataSource;

  private StatsDumperPrometheus(StatsDumperDataSource dataSource) {
    mDataSource = dataSource;
  }

  /**
   * Schedules a new stats task.
   *
   * @param dataSource the data source
   * @param intervalMillis interval between writes. The first write is delayed by this interval.
   */
  public static void schedule(final StatsDumperDataSource dataSource, final long intervalMillis) {
    // Stop using TaskScheduler (bug 22978)
    final StatsDumperPrometheus dumper = new StatsDumperPrometheus(dataSource);
    Runnable r =
        () -> {
          while (true) {
            try {
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
        };
    new Thread(statsGroup, r, dataSource.getFilename()).start();
  }

  private File getFile() throws IOException {
    FileUtil.ensureDirExists(STATS_DIR);
    return new File(STATS_DIR, mDataSource.getFilename());
  }

  /** Gets the latest data from the data source and writes it to the file. */
  public Void call() throws Exception {
    final Collection<String> dataLines = mDataSource.getDataLines();
    if (dataLines == null || dataLines.size() == 0) {
      return null;
    }

    // stat prefix
    final String statFileName = mDataSource.getFilename();
    final String statFilePrefix =
        statFileName.lastIndexOf(".") == -1
            ? statFileName
            : statFileName.substring(0, statFileName.lastIndexOf("."));

    // headers array
    final String[] headers = mDataSource.getHeader().split(",");

    // timestamp processing
    StringBuilder logBuffer = new StringBuilder();
    if (mDataSource.hasTimestampColumn()
        && !logBuffer.toString().contains(statFilePrefix + "_timestamp")) {
      logBuffer
          .append(statFilePrefix)
          .append("_")
          .append("timestamp")
          .append(" ")
          .append(Instant.now().toEpochMilli())
          .append("\n");
    }

    // metrics processing
    for (String line : dataLines) {
      final String[] stats = line.split(",");
      if (statFilePrefix.equals("soap")
          || statFilePrefix.equals("imap")
          || statFilePrefix.equals("ldap")) {
        processComplexStat(statFilePrefix, logBuffer, headers, stats);
      } else {
        for (int i = 0; i < stats.length; i++) {
          logBuffer
              .append(statFilePrefix)
              .append("_")
              .append(headers[i])
              .append(" ")
              .append(stats[i])
              .append("\n");
        }
      }
    }

    // write log to prom file and close
    writeLogBuffer(logBuffer);
    return null;
  }

  private void processComplexStat(
      String statFilePrefix, StringBuilder logBuffer, final String[] headers, String[] stats) {
    /*
     Input:
       GetAllServersRequest,1,3
     Output:
       soap_exec_count {request="GetMsgRequest"} 1
       soap_exec_ms_avg {request="GetMsgRequest"} 11
    */
    final String command = stats[0];
    final String header = headers[0];
    for (int i = 1; i < headers.length; i++) {
      logBuffer
          .append(statFilePrefix)
          .append("_")
          .append(headers[i])
          .append(" ")
          .append(" {")
          .append(header)
          .append("=\"")
          .append(command)
          .append("\"}")
          .append(" ")
          .append(stats[i])
          .append("\n");
    }
  }

  private void writeLogBuffer(final StringBuilder logBuffer) throws IOException {
    File file = getFile();
    FileWriter writer = new FileWriter(file, false);
    writer.write(logBuffer.toString());
    writer.close();
  }
}

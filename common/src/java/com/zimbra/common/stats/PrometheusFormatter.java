package com.zimbra.common.stats;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.IntStream;

/**
 * A Utility class to format stats into prometheus format
 *
 * @author Keshav Bhatt
 * @since 4.0.7
 */
public class PrometheusFormatter {

  /**
   * Process the complex metrics type
   *
   * @param statFilePrefix stat filename prefix
   * @param logBuffer the logBuffer to which the processed data will pe appended
   * @param headers headers of the dataSource {@link StatsDumperDataSource}
   * @param stats stats collected from dataSource {@link StatsDumperDataSource}
   */
  static void processComplexStats(
      String statFilePrefix, StringBuilder logBuffer, final String[] headers, String[] stats) {
    final String command = stats[0];
    final String header = headers[0];
    IntStream.range(1, headers.length)
        .forEach(
            i ->
                logBuffer
                    .append(statFilePrefix)
                    .append("_")
                    .append(sanitizeHeader(headers[i]))
                    .append(" ")
                    .append("{")
                    .append(header)
                    .append("=\"")
                    .append(command)
                    .append("\"}")
                    .append(" ")
                    .append("".equals(stats[i]) ? "0" : stats[i])
                    .append("\n"));
  }

  /**
   * Process the normal metrics type
   *
   * @param statFilePrefix stat filename prefix
   * @param logBuffer the logBuffer to which the processed data will pe appended
   * @param headers headers of the dataSource {@link StatsDumperDataSource}
   * @param stats stats collected from dataSource {@link StatsDumperDataSource}
   */
  static void processNormalStats(
      String statFilePrefix, StringBuilder logBuffer, final String[] headers, String[] stats) {
    IntStream.range(0, stats.length)
        .forEach(
            i ->
                logBuffer
                    .append(statFilePrefix)
                    .append("_")
                    .append(sanitizeHeader(headers[i]))
                    .append(" ")
                    .append(("".equals(stats[i]) ? "0" : stats[i]))
                    .append("\n"));
  }

  /**
   * Clean up the header to make them compatible for export
   *
   * @param headerStr header {@link String}
   * @return sanitized header {@link String}
   */
  static String sanitizeHeader(String headerStr) {
    headerStr = headerStr.replace("'", "");
    headerStr = headerStr.replace("-", "_");
    return headerStr;
  }

  static void addTimeStamp(String statFilePrefix, StringBuilder logBuffer, long timestamp) {
    if (logBuffer.toString().contains(statFilePrefix + "_last_extraction_timestamp")) return;
    logBuffer
        .append(statFilePrefix)
        .append("_last_extraction_timestamp")
        .append(" ")
        .append(timestamp)
        .append("\n");
  }

  /**
   * Format data present in data source into format compatible with prometheus
   *
   * @param mDataSource datasource for concerned stat{@link StatsDumperDataSource}
   * @return {@link StringBuilder} or null if the data source {@link StatsDumperDataSource} contains
   *     no data
   */
  public static StringBuilder format(StatsDumperDataSource mDataSource) {
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
    final long timestamp = Instant.now().toEpochMilli();
    if (mDataSource.hasTimestampColumn()) {
      addTimeStamp(statFilePrefix, logBuffer, timestamp);
    }

    // metrics processing
    for (String line : dataLines) {
      final String[] stats = line.split(",");
      if ("command".equals(headers[0])) {
        processComplexStats(statFilePrefix, logBuffer, headers, stats);
      } else {
        processNormalStats(statFilePrefix, logBuffer, headers, stats);
      }
    }
    return logBuffer;
  }
}

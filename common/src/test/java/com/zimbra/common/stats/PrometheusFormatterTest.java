package com.zimbra.common.stats;

import static org.junit.Assert.assertEquals;

import java.util.stream.IntStream;
import org.junit.Test;

public class PrometheusFormatterTest {

  @Test
  public void shouldFormatCorrectlyWhenCalledProcessComplexStat() {

    StringBuilder buf = new StringBuilder();
    String[] header = {"command", "exec_count", "exec_ms_avg"};
    String[] stats = {"NoOpRequest", "2", "0"};

    PrometheusFormatter.processComplexStats("soap", buf, header, stats);

    String expected =
        "soap_exec_count {command=\"NoOpRequest\"} 2\n"
            + "soap_exec_ms_avg {command=\"NoOpRequest\"} 0\n";

    assertEquals(expected, buf.toString());
  }

  @Test
  public void shouldCleanHeaderWhenCalledSanitizeHeader() {
    String[] headers = {"command", "exec-count", "exec_ms-avg", "exec's_duration"};
    StringBuilder buf = new StringBuilder();
    IntStream.range(1, headers.length)
        .forEach(i -> buf.append(PrometheusFormatter.sanitizeHeader(headers[i])).append(","));
    String expected = "exec_count,exec_ms_avg,execs_duration,";
    assertEquals(expected, buf.toString());
  }

  @Test
  public void shouldFormatCorrectlyWhenCalledProcessNormalStats() {
    String[] headers = {
      "AnonymousIoService",
      "CloudRoutingReaderThread",
      "GC",
      "ImapSSLServer",
      "ImapServer",
      "LmtpServer",
      "Pop3SSLServer",
      "Pop3Server",
      "ScheduledTask",
      "SocketAcceptor",
      "Thread",
      "Timer",
      "btpool",
      "pool"
    };

    String[] stats = {"0", "0", "0", "0", "0", "2", "0", "0", "10", "0", "2", "5", "0", "0"};

    StringBuilder buf = new StringBuilder();
    PrometheusFormatter.processNormalStats("threads", buf, headers, stats);
    String expected =
        "threads_AnonymousIoService 0\n"
            + "threads_CloudRoutingReaderThread 0\n"
            + "threads_GC 0\n"
            + "threads_ImapSSLServer 0\n"
            + "threads_ImapServer 0\n"
            + "threads_LmtpServer 2\n"
            + "threads_Pop3SSLServer 0\n"
            + "threads_Pop3Server 0\n"
            + "threads_ScheduledTask 10\n"
            + "threads_SocketAcceptor 0\n"
            + "threads_Thread 2\n"
            + "threads_Timer 5\n"
            + "threads_btpool 0\n"
            + "threads_pool 0\n";
    assertEquals(expected, buf.toString());
  }

  @Test
  public void shouldAddTimestampWhenCalledAddTimeStamp() {
    final long timestamp = 1651670178388L;
    var logBuffer = new StringBuilder();
    PrometheusFormatter.addTimeStamp("sql", logBuffer, timestamp);
    var expected = "sql_last_extraction_timestamp 1651670178388\n";
    assertEquals(expected, logBuffer.toString());
  }
}

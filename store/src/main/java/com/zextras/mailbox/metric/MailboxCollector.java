/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zextras.mailbox.metric;

import com.zimbra.cs.stats.ZimbraPerf;
import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.SummaryMetricFamily;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Collects statistics about core Mailbox functionalities: IMAP, POP, LMTP and SMTP
 *
 * @since 23.4.0
 * @author davidefrison
 */
public class MailboxCollector extends Collector {
  private final SummaryMetricFamily imapSummary =
      new SummaryMetricFamily(
          "imap_exec_ms", "Summary of IMAP request duration in millisecond", List.of("command"));
  private final SummaryMetricFamily popSummary =
      new SummaryMetricFamily(
          "pop_exec_ms", "Summary of IMAP request duration in millisecond", List.of("command"));
  private final CounterMetricFamily lmtpRcvdMsgs =
      new CounterMetricFamily("lmtp_rcvd_msgs", "LMTP received messages", List.of());
  private final CounterMetricFamily lmtpRcvdBytes =
      new CounterMetricFamily("lmtp_rcvd_bytes", "LMTP received bytes", List.of());
  private final CounterMetricFamily lmtpRcvdRcpt =
      new CounterMetricFamily("lmtp_rcvd_rcpt", "LMTP received receipt", List.of());
  private final CounterMetricFamily lmtpDlvdMsgs =
      new CounterMetricFamily("lmtp_dlvd_msgs", "LMTP delivered msgs", List.of());
  private final CounterMetricFamily lmtpDlvdBytes =
      new CounterMetricFamily("lmtp_dlvd_bytes", "LMTP delivered bytes", List.of("command"));

  @Override
  public List<MetricFamilySamples> collect() {
    // TODO: add metrics on threads + latency on POP, IMAP, LMTP calls
    return new ArrayList<>() {
      {
        addAll(getLmtpMetrics());
        addAll(getRealTimeStats());
      }
    };
  }

  /**
   * Returns all collected LMTP metrics about threads, rcvd, dlvd
   *
   * @return collection of LMTP metrics
   */
  private Collection<MetricFamilySamples> getLmtpMetrics() {
    return new ArrayList<>() {
      {
        add(
            new CounterMetricFamily(
                ZimbraPerf.DC_LMTP_RCVD_MSGS,
                "LMTP received messages",
                ZimbraPerf.COUNTER_LMTP_RCVD_MSGS.getTotal()));
        add(
            new CounterMetricFamily(
                ZimbraPerf.DC_LMTP_RCVD_BYTES,
                "LMTP received bytes",
                ZimbraPerf.COUNTER_LMTP_RCVD_BYTES.getTotal()));
        add(
            new CounterMetricFamily(
                ZimbraPerf.DC_LMTP_RCVD_RCPT,
                "LMTP received receipt",
                ZimbraPerf.COUNTER_LMTP_RCVD_RCPT.getTotal()));
        add(
            new CounterMetricFamily(
                ZimbraPerf.DC_LMTP_DLVD_BYTES,
                "LMTP delivered bytes",
                ZimbraPerf.COUNTER_LMTP_DLVD_BYTES.getTotal()));
        add(
            new CounterMetricFamily(
                ZimbraPerf.DC_LMTP_DLVD_MSGS,
                "LMTP delivered bytes",
                ZimbraPerf.COUNTER_LMTP_DLVD_MSGS.getTotal()));
      }
    };
  }

  private Collection<MetricFamilySamples> getImapMetrics() {
    ZimbraPerf.IMAP_TRACKER_PROMETHEUS
        .getCounters()
        .forEach(
            (label, counter) ->
                imapSummary.addMetric(List.of(label), counter.getCount(), counter.getTotal()));
    return List.of(imapSummary);
  }

  /**
   * Returns statistics about "real time data". See al classes that implement {@link
   * com.zimbra.common.stats.RealtimeStatsCallback} to get an overview of the data that can be
   * returned.
   *
   * @return
   */
  private Collection<MetricFamilySamples> getRealTimeStats() {
    Collection<MetricFamilySamples> metricFamilySamples = new ArrayList<>();
    ZimbraPerf.getRealTimeStats()
        .forEach(
            (metricName, value) ->
                metricFamilySamples.add(new GaugeMetricFamily(metricName, "", value)));
    return metricFamilySamples;
  }
}

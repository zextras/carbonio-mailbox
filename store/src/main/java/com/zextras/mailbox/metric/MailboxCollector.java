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
 * Collects statistics about core Mailbox functionalities: IMAP, POP, LMTP
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
        addAll(getRealTimeStats());
        addAll(getLmtpMetrics());
        addAll(getImapMetrics());
        addAll(getPopMetrics());
      }
    };
  }

  /**
   * Returns collected LMTP metrics on rcvd, dlvd messages, bytes and rcpt.
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

  /**
   * Adds IMAP metrics about IMAP calls response time.
   *
   * @return imap metrics
   */
  private Collection<MetricFamilySamples> getImapMetrics() {
    ZimbraPerf.IMAP_TRACKER_PROMETHEUS
        .getCounters()
        .forEach(
            (label, counter) ->
                imapSummary.addMetric(List.of(label), counter.getCount(), counter.getTotal()));
    return List.of(imapSummary);
  }

  /**
   * Adds IMAP metrics about IMAP calls response time.
   *
   * @return pop metrics
   */
  private Collection<MetricFamilySamples> getPopMetrics() {
    ZimbraPerf.POP_TRACKER_PROMETHEUS
        .getCounters()
        .forEach(
            (label, counter) ->
                popSummary.addMetric(List.of(label), counter.getCount(), counter.getTotal()));
    return List.of(popSummary);
  }

  /**
   * Returns statistics about "real time data". See al classes that implement {@link
   * com.zimbra.common.stats.RealtimeStatsCallback} to get an overview of the data that can be
   * returned. These stats are mostly about LMTP, IMAP and POP threads
   *
   * @return metrics about real time data
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

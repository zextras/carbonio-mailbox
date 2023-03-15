/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zextras.mailbox.metric;

import com.zimbra.cs.stats.ZimbraPerf;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;

public class Metrics {
  public static final CollectorRegistry COLLECTOR_REGISTRY = new CollectorRegistry();

  public static final MeterRegistry METER_REGISTRY =
      new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, COLLECTOR_REGISTRY, Clock.SYSTEM);

  // LMTP
  public static final Counter LMTP_DLVD_BYTES_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_LMTP_DLVD_BYTES);
  public static final Counter LMTP_DLVD_MSGS_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_LMTP_DLVD_MSGS);
  public static final Counter LMTP_RCVD_BYTES_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_LMTP_RCVD_BYTES);
  public static final Counter LMTP_RCVD_MSGS_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_LMTP_RCVD_MSGS);
  public static final Counter LMTP_RCVD_RCPT_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_LMTP_RCVD_RCPT);

  // Calendar
  public static final Counter CALENDAR_CACHE_HIT_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_CALCACHE_HIT);
  public static final Counter CALENDAR_CACHE_MEM_HIT_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_CALCACHE_MEM_HIT);
  public static final Counter CALENDAR_LRU_SIZE_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_CALCACHE_LRU_SIZE);

  // Lucene
  public static final Counter IDX_WRT_OPENED_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_IDX_WRT_OPENED);
  public static final Counter IDX_BYTES_READ_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_IDX_BYTES_READ);
  public static final Counter IDX_BYTES_WRITTEN_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_IDX_BYTES_WRITTEN);
  public static final Counter IDX_WRT_OPENED_CACHE_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_IDX_WRT_OPENED_CACHE_HIT);

  // Files
  public static final Counter BLOB_INPUT_STREAM_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_BIS_READ);
  public static final Counter BLOB_SEEK_RATE_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_BIS_SEEK_RATE);

  // Timers aka STOPWATCH
  // DB connections
  public static final Timer DB_CONN_TIMER = METER_REGISTRY.timer("db_conn_exec_ms");

  // Pop
  public static final Timer POP_REQUEST_TIMER = METER_REGISTRY.timer("pop_exec_ms");

  // Imap
  public static final Timer IMAP_REQUEST_TIMER = METER_REGISTRY.timer("imap_exec_ms");

  // LDAP
  public static final Timer LDAP_REQUEST_TIMER = METER_REGISTRY.timer("ldap_exec_ms");
}

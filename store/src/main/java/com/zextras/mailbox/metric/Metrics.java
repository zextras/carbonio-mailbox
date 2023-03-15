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

/**
 * Defines registry for metrics.
 * It is used mainly because other Servlets and classes do not have injection, otherwise
 * {@link this#COLLECTOR_REGISTRY} would have been an injected instance.
 *
 * @since 23.4.0
 * @author davidefrison
 */
public class Metrics {
  public static final CollectorRegistry COLLECTOR_REGISTRY = new CollectorRegistry();

  public static final MeterRegistry METER_REGISTRY =
      new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, COLLECTOR_REGISTRY, Clock.SYSTEM);

  // Lucene (not used for some reason)
  public static final Counter IDX_WRT_OPENED_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_IDX_WRT_OPENED);
  public static final Counter IDX_BYTES_READ_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_IDX_BYTES_READ);
  public static final Counter IDX_BYTES_WRITTEN_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_IDX_BYTES_WRITTEN);
  public static final Counter IDX_WRT_OPENED_CACHE_COUNTER =
      METER_REGISTRY.counter(ZimbraPerf.DC_IDX_WRT_OPENED_CACHE_HIT);

  // Timers aka STOPWATCH
  // DB connections
  public static final Timer DB_CONN_TIMER = METER_REGISTRY.timer("db_conn_exec_ms");

  // Pop
  public static final Timer POP_REQUEST_TIMER = METER_REGISTRY.timer("pop_exec_ms");

  // Imap
  public static final Timer IMAP_REQUEST_TIMER = METER_REGISTRY.timer("imap_exec_ms");

  // LDAP
  public static final Timer LDAP_REQUEST_TIMER = METER_REGISTRY.timer("ldap_exec_ms");

  // Threads and connections
}

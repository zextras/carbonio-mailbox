/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zextras.mailbox.metric;

import com.zimbra.cs.stats.ZimbraPerf;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class Metrics {

  public static final MeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
  public static final Counter imapCounter = registry.counter("imap_");

  // LMTP
  public static final Counter LMTP_DLVD_BYTES_COUNTER = registry.counter(ZimbraPerf.DC_LMTP_DLVD_BYTES);
  public static final Counter LMTP_DLVD_MSGS_COUNTER = registry.counter(ZimbraPerf.DC_LMTP_DLVD_MSGS);
  public static final Counter LMTP_RCVD_BYTES_COUNTER = registry.counter(ZimbraPerf.DC_LMTP_RCVD_BYTES);
  public static final Counter LMTP_RCVD_MSGS_COUNTER = registry.counter(ZimbraPerf.DC_LMTP_RCVD_MSGS);

  // Calendar
  public static final Counter CALENDAR_CACHE_HIT_COUNTER = registry.counter(ZimbraPerf.DC_CALCACHE_HIT);
  public static final Counter CALENDAR_MEM_HIT_COUNTER = registry.counter(ZimbraPerf.DC_CALCACHE_MEM_HIT);
  public static final Counter CALENDAR_LRU_SIZE_COUNTER = registry.counter(ZimbraPerf.DC_CALCACHE_LRU_SIZE);

  // Lucene
  public static final Counter IDX_WRT_OPENED_COUNTER = registry.counter(ZimbraPerf.DC_IDX_WRT_OPENED);
  public static final Counter IDX_BYTES_READ_COUNTER = registry.counter(ZimbraPerf.DC_IDX_BYTES_READ);
  public static final Counter IDX_BYTES_WRITTEN_COUNTER = registry.counter(ZimbraPerf.DC_IDX_BYTES_WRITTEN);
  public static final Counter IDX_WRT_OPENED_CACHE_COUNTER = registry.counter(ZimbraPerf.DC_IDX_WRT_OPENED_CACHE_HIT);

  // Files
  public static final Counter BLOB_INPUT_STREAM_COUNTER = registry.counter(ZimbraPerf.DC_BIS_READ);
  public static final Counter BLOB_SEEK_RATE_COUNTER = registry.counter(ZimbraPerf.DC_BIS_SEEK_RATE);

  // Pop
  public static final Timer POP_TIMER = registry.timer("pop_exec_ms");

  // Imap
  public static final Timer IMAP_TIMER = registry.timer("imap_exec_ms");

}

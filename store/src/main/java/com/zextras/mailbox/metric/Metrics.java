/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zextras.mailbox.metric;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
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

  /**
   * Binds a Prometheus-compatible registry to the registry
   */
  public static final MeterRegistry METER_REGISTRY =
      new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, COLLECTOR_REGISTRY, Clock.SYSTEM);
}

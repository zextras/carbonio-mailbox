/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zextras.mailbox.servlet;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;
import com.zextras.mailbox.metric.Metrics;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import javax.inject.Singleton;

public class MetricsServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/metrics").with(MetricsServlet.class);
  }

  /**
   * Provides prometheus {@link MetricsServlet} with the defined collector registry.
   *
   * @param collectorRegistry the registry holding collectors
   * @return metrics servlet for the mailbox application
   */
  @Provides
  @Singleton
  public MetricsServlet provideMetricsServlet(CollectorRegistry collectorRegistry) {
    return new MetricsServlet(collectorRegistry);
  }

  /**
   * Provides the {@link CollectorRegistry#defaultRegistry} for collection of stats and registers
   * collectors in it. It also registers standard collectors using {@link DefaultExports#register}.
   *
   * @return registry for prometheus
   */
  @Provides
  @Singleton
  public CollectorRegistry provideCollector() {
    // TODO: it would be nice to get all collectors and register them programmatically
    final CollectorRegistry metricRegistry = Metrics.COLLECTOR_REGISTRY;
    DefaultExports.register(metricRegistry);
    return metricRegistry;
  }
}

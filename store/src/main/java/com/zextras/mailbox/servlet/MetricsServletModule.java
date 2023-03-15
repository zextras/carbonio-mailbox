/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zextras.mailbox.servlet;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;
import com.zextras.mailbox.metric.Metrics;
import com.zextras.mailbox.metric.SoapCollector;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import javax.inject.Named;
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
   * Provides the {@link SoapCollector} for soap metrics.
   *
   * @return soap api collector
   */
  @Provides
  @Singleton
  @Named("SoapCollector")
  public Collector provideSoapCollector() {
    return new SoapCollector();
  }

  /**
   * Provides the {@link CollectorRegistry#defaultRegistry} for collection of stats and registers
   * collectors in it. It also registers standard collectors using {@link DefaultExports#register}.
   *
   * @param soapCollector collector for soap api metrics
   * @return registry for prometheus
   */
  @Provides
  @Singleton
  public CollectorRegistry provideCollector(@Named("SoapCollector") Collector soapCollector) {
    // TODO: it would be nice to get all collectors and register them programmatically
    final CollectorRegistry metricRegistry = Metrics.COLLECTOR_REGISTRY;
    DefaultExports.register(metricRegistry);
    metricRegistry.register(soapCollector);
    return metricRegistry;
  }
}

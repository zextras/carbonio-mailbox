/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zextras.mailbox.servlet;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;
import com.zextras.mailbox.metric.SoapCollector;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.client.jetty.JettyStatisticsCollector;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.jetty.server.handler.StatisticsHandler;

public class MetricsServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/metrics").with(MetricsServlet.class);
  }

  /**
   * Provides prometheus {@link MetricsServlet} with the defined collector registry.
   *
   * @param collectorRegistry thge registry holding collectors
   * @return
   */
  @Provides
  @Singleton
  public MetricsServlet provideMetricsServlet(CollectorRegistry collectorRegistry) {
    return new MetricsServlet(collectorRegistry);
  }

  /**
   * Provides the {@link SoapCollector} for soap metrics.
   *
   * @return
   */
  @Provides
  @Singleton
  @Named("SoapCollector")
  public Collector provideSoapCollector() {
    return new SoapCollector();
  }

  @Provides
  @Singleton
  @Named("ProcessCollector")
  public Collector provideProcessCollector(StatisticsHandler statisticsHandler) {
    return new JettyStatisticsCollector(statisticsHandler);
  }

  /**
   * Provides the {@link CollectorRegistry#defaultRegistry} for collection of stats and registers
   * provided collectors to it. It also registers standard collectors using {@link
   * DefaultExports#initialize()}.
   *
   * @param processCollector collector for jetty process metrics
   * @param soapCollector collector for soap api metrics
   * @return registry for prometheus
   */
  @Provides
  @Singleton
  public CollectorRegistry provideCollector(
      @Named("ProcessCollector") Collector processCollector,
      @Named("SoapCollector") Collector soapCollector) {
    final CollectorRegistry metricRegistry = CollectorRegistry.defaultRegistry;
    DefaultExports.initialize();
    metricRegistry.register(processCollector);
    metricRegistry.register(soapCollector);
    return metricRegistry;
  }
}

/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zextras.mailbox.servlet;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;
import com.zextras.mailbox.metric.CarbonioMetricRegisterer;
import com.zextras.mailbox.metric.Metrics;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public class MetricsServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/metrics").with(PrometheusMetricsServlet.class);
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
    CarbonioMetricRegisterer.register(Metrics.COLLECTOR_REGISTRY);
    return Metrics.COLLECTOR_REGISTRY;
  }

  /**
   * Jakarta-compatible Prometheus metrics servlet that writes metrics in text format.
   */
  public static class PrometheusMetricsServlet extends HttpServlet {
    private final CollectorRegistry registry;

    @Inject
    public PrometheusMetricsServlet(CollectorRegistry registry) {
      this.registry = registry;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
      resp.setStatus(200);
      resp.setContentType(TextFormat.CONTENT_TYPE_004);
      try (Writer writer = resp.getWriter()) {
        TextFormat.write004(writer, registry.metricFamilySamples());
      }
    }
  }

}

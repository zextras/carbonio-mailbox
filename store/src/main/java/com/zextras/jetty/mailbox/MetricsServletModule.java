package com.zextras.jetty.mailbox;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.google.inject.servlet.ServletModule;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.MetricsServlet;
import java.util.concurrent.TimeUnit;

public class MetricsServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    final MetricRegistry metricRegistry = new MetricRegistry();
    metricRegistry.register("gc", new GarbageCollectorMetricSet());
    metricRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
    metricRegistry.register("memory", new MemoryUsageGaugeSet());
    // TODO: register other metrics
    final DropwizardExports dropwizardExports = new DropwizardExports(metricRegistry);
    CollectorRegistry.defaultRegistry.register(dropwizardExports);
    serve("/metrics").with(new MetricsServlet());
  }
}

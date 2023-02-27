package com.zextras.jetty.mailbox;

import com.google.inject.servlet.ServletModule;
import com.zextras.metric.mailbox.SoapCollector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;

public class MetricsServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    CollectorRegistry.defaultRegistry.register(new SoapCollector());
    serve("/metrics").with(new MetricsServlet());
  }
}

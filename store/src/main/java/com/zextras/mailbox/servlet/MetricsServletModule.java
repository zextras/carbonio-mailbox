package com.zextras.mailbox.servlet;

import com.google.inject.servlet.ServletModule;
import com.zextras.mailbox.metric.SoapCollector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;

public class MetricsServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    CollectorRegistry.defaultRegistry.register(new SoapCollector());
    serve("/metrics").with(new MetricsServlet());
  }
}

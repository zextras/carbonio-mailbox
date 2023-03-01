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
    this.getServletContext().getServerInfo();
  }

  @Provides
  @Singleton
  public MetricsServlet provideMetricsServlet(CollectorRegistry collectorRegistry) {
    return new MetricsServlet(collectorRegistry);
  }

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
   * Provides the registry for collection of stats. It also binds collectors to it. NOTE: This
   * registers jmx metrics to the default exports Technically it would be better to register all
   * hotspot Collectors to our registry but here we are using the default registry, so we are good.
   *
   * @param processCollector
   * @param soapCollector
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

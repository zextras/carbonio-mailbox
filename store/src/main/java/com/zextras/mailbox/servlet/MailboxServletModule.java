/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zextras.mailbox.servlet;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;
import com.zextras.mailbox.metric.Metrics;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.AdminSoapServlet;
import com.zimbra.soap.UserSoapServlet;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;

public class MailboxServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/metrics").with(MetricsServlet.class);
    serve("/soap/*").with(UserSoapServlet.class);
    serve("/admin/soap/*").with(AdminSoapServlet.class);
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

  @Provides
  @Singleton
  public Provisioning provideProvisioning() {
    return Provisioning.getInstance();
  }

  @Provides
  @Singleton
  @Named("adminSOAPPorts")
  public List<Integer> provideAdminSOAPPorts(Provisioning provisioning) throws ServiceException {
    final int adminPort = provisioning.getConfig().getAdminPort();
    final int mtaAuthPort = provisioning.getConfig().getMtaAuthPort();
    return List.of(adminPort, mtaAuthPort, 7071, 7073);
  }

  @Provides
  @Singleton
  @Named("userSOAPPorts")
  public List<Integer> provideUserSOAPPorts(Provisioning provisioning) throws ServiceException {
    final int mailSSLPort = provisioning.getConfig().getMailSSLPort();
    final int mailPort = provisioning.getConfig().getMailPort();
    return List.of(mailPort, mailSSLPort, 7070, 7443);
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

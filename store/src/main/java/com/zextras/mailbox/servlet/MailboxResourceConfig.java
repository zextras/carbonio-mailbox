// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zextras.mailbox.filter.AuthorizationFilter;
import javax.inject.Inject;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;

/**
 * Bridges Guice to Jersey DI system
 *
 * @author davidefrison
 */
public class MailboxResourceConfig extends ResourceConfig {

  @Inject
  public MailboxResourceConfig(ServiceLocator serviceLocator, AbstractModule abstractModule) {
    packages("com.zextras.mailbox.resource");
    register(AuthorizationFilter.class);
    Injector injector = Guice.createInjector(abstractModule);
    initGuiceIntoHK2Bridge(serviceLocator, injector);
  }

  private void initGuiceIntoHK2Bridge(ServiceLocator serviceLocator, Injector injector) {
    GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
    GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
    guiceBridge.bridgeGuiceInjector(injector);
  }
}

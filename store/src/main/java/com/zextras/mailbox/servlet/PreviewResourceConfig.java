// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zextras.mailbox.filter.AuthorizationFilter;
import com.zextras.mailbox.preview.resource.PreviewController;
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
public class PreviewResourceConfig extends ResourceConfig {

  @Inject
  public PreviewResourceConfig(ServiceLocator serviceLocator) {
    register(PreviewController.class);
    register(AuthorizationFilter.class);
    Injector injector = Guice.createInjector(new PreviewServletModule());
    initGuiceIntoHK2Bridge(serviceLocator, injector);
  }

  private void initGuiceIntoHK2Bridge(ServiceLocator serviceLocator, Injector injector) {
    GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
    GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
    guiceBridge.bridgeGuiceInjector(injector);
  }
}

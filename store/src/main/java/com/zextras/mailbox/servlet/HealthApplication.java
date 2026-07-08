// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application for the health endpoint. Only lists the resource class — CDI/Weld builds the
 * instance (embedded Jetty does not run RESTEasy's classpath scanning, so the class is registered
 * explicitly here).
 */
public class HealthApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return Set.of(HealthResource.class);
  }
}

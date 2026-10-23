// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import com.zextras.mailbox.health.DatabaseServiceDependency;
import com.zextras.mailbox.health.HealthUseCase;
import com.zimbra.cs.db.DbPool;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Application;

/** JAX-RS application for the health endpoint (embedded Jetty does no classpath scanning). */
public class HealthApplication extends Application {

  private final Set<Object> singletons =
      Set.of(
          new HealthResource(
              new HealthUseCase(
                  List.of(new DatabaseServiceDependency(DbPool.global(), System::currentTimeMillis)))));

  @Override
  public Set<Object> getSingletons() {
    return singletons;
  }
}

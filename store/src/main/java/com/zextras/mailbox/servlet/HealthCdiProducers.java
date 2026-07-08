// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import com.zextras.mailbox.health.DatabaseServiceDependency;
import com.zextras.mailbox.health.HealthUseCase;
import com.zimbra.cs.db.DbPool;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

/** CDI producers for the health layer. */
@ApplicationScoped
public class HealthCdiProducers {

  @Produces
  @Singleton
  public HealthUseCase healthUseCase() {
    return new HealthUseCase(
        List.of(new DatabaseServiceDependency(DbPool.global(), System::currentTimeMillis)));
  }
}

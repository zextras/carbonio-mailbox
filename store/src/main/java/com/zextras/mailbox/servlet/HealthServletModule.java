// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;
import com.zextras.mailbox.health.DatabaseServiceDependency;
import com.zextras.mailbox.health.HealthUseCase;
import com.zextras.mailbox.health.ServiceDependency;
import com.zimbra.cs.db.DbPool;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;

public class HealthServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/health", "/health/*").with(HealthServlet.class);
  }

  @Provides
  @Singleton
  DbPool provideDatabasePool() {
    return new DbPool();
  }

  @Provides
  @Singleton
  HealthUseCase provideHealthService(DbPool dbPool) {
    List <ServiceDependency> serviceDependencies = new ArrayList<>();
    serviceDependencies.add(new DatabaseServiceDependency(dbPool, System::currentTimeMillis));

    return new HealthUseCase(serviceDependencies);
  }
}

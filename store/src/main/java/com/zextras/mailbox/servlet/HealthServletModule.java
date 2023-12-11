// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;
import com.zextras.mailbox.health.DatabaseServiceDependency;
import com.zextras.mailbox.health.HealthUseCase;
import com.zimbra.cs.db.DbPool;
import java.util.List;
import java.util.function.Supplier;
import javax.inject.Named;
import javax.inject.Singleton;

public class HealthServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/health", "/health/*").with(HealthServlet.class);
  }


  @Singleton
  DbPool provideDatabasePool() {
    return new DbPool();
  }

  @Provides
  @Named("currentTimeSupplier")
  Supplier<Long> provideCurrentTimeSupplier() {
    return System::currentTimeMillis;
  }


  @Provides
  @Singleton
  HealthUseCase provideHealthService(DbPool dbPool, @Named("currentTimeSupplier") Supplier<Long> currentTimeSupplier) {
    return new HealthUseCase(
        List.of(new DatabaseServiceDependency(dbPool, currentTimeSupplier)));
  }

}

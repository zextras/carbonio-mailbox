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

public class HealthServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/health", "/health/*").with(HealthServlet.class);
  }

  @Provides
  HealthUseCase provideHealthService() {
    return new HealthUseCase(
        List.of(new DatabaseServiceDependency(new DbPool(), System::currentTimeMillis)));
  }

}

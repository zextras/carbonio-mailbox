// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;
import com.zextras.mailbox.health.DatabaseService;
import com.zextras.mailbox.health.HealthService;
import com.zimbra.cs.db.DbPool;
import java.util.List;

public class HealthServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/health", "/health/*").with(HealthServlet.class);
  }

  @Provides
  HealthService provideHealthService() {
    return new HealthService(
        List.of(new DatabaseService(new DbPool())));
  }

}

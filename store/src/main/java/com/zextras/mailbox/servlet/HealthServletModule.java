// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.servlet.ServletModule;

public class HealthServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/health/live").with(HealthLiveServlet.class);
  }

}

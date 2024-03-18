// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.servlet;

import com.google.inject.Binder;
import com.google.inject.Module;

public class RestServletModule implements Module {

  @Override
  public void configure(Binder binder) {
    binder.bind(RestServlet.class);
  }
}

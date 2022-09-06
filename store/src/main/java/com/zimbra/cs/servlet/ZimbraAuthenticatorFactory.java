// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import javax.servlet.ServletContext;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.Authenticator.AuthConfiguration;
import org.eclipse.jetty.security.DefaultAuthenticatorFactory;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Server;

/**
 * Jetty Authenticator Factory which adds support for 'ZimbraAuth' mechanism using Zimbra auth
 * tokens
 */
public class ZimbraAuthenticatorFactory extends DefaultAuthenticatorFactory {

  public static String ZIMBRA_AUTH_MECHANISM = "ZimbraAuth";

  private ZimbraAuthenticator zimbraAuthenticator = new ZimbraAuthenticator();

  public void setUrlPattern(String pattern) {
    zimbraAuthenticator.setUrlPattern(pattern);
  }

  @Override
  public Authenticator getAuthenticator(
      Server server,
      ServletContext context,
      AuthConfiguration configuration,
      IdentityService identityService,
      LoginService loginService) {
    String auth = configuration.getAuthMethod();
    if (ZIMBRA_AUTH_MECHANISM.equalsIgnoreCase(auth)) {
      return zimbraAuthenticator;
    } else {
      return super.getAuthenticator(server, context, configuration, identityService, loginService);
    }
  }
}

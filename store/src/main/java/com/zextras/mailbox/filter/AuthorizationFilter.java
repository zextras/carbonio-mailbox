// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.filter;

import static com.zimbra.common.util.ZimbraCookie.COOKIE_ZM_AUTH_TOKEN;

import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Authorization filter for Cookie authorization. Uses {@link AuthToken#getAuthToken(String)} to
 * validate the cookie.
 *
 * @author davidefrison
 */
@Provider
public class AuthorizationFilter implements ContainerRequestFilter {

  public static final String CTX_AUTH_TOKEN = "authToken";

  @Override
  public void filter(ContainerRequestContext ctx) throws IOException {
    final Map<String, Cookie> cookies = ctx.getCookies();
    if (!(Objects.equals(cookies, null))) {
      final Cookie authCookie = cookies.get(COOKIE_ZM_AUTH_TOKEN);
      if (!Objects.isNull(authCookie)) {
        try {
          final AuthToken authToken = AuthToken.getAuthToken(authCookie.getValue());
          ctx.setProperty(CTX_AUTH_TOKEN, authToken);
          return;
        } catch (AuthTokenException e) {
          // TODO: log
        }
      }
    }
    ctx.abortWith(Response.status(Response.Status.FORBIDDEN).entity("Cannot access").build());
  }
}

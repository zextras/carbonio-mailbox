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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authorization filter for Cookie authorization. Uses {@link AuthToken#getAuthToken(String)} to
 * validate the cookie. Adds a {@link AuthToken} to the context {@link ContainerRequestContext} if
 * authorization successful
 *
 * @author davidefrison
 */
@Provider
public class AuthorizationFilter implements ContainerRequestFilter {

  public static final String CTX_AUTH_TOKEN = "authToken";
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);

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
          LOGGER.debug("Failed to get auth token: " + e.getMessage());
        }
      }
    }
    ctx.abortWith(Response.status(Response.Status.FORBIDDEN).entity("Cannot access").build());
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import com.zimbra.common.localconfig.LC;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ZimbraCookie {

  public static final String COOKIE_ZM_AUTH_TOKEN = "ZM_AUTH_TOKEN";
  public static final String COOKIE_ZM_ADMIN_AUTH_TOKEN = "ZM_ADMIN_AUTH_TOKEN";
  public static final String COOKIE_ZM_TRUST_TOKEN = "ZM_TRUST_TOKEN";
  public static final String COOKIE_ZM_JWT = "ZM_JWT";
  public static String PATH_ROOT = "/";

  public static String authTokenCookieName(boolean isAdminReq) {
    return isAdminReq ? COOKIE_ZM_ADMIN_AUTH_TOKEN : COOKIE_ZM_AUTH_TOKEN;
  }

  /**
   * set cookie domain and path for the cookie going back to the browser
   *
   * @param cookie the cookie
   * @param path path in domain the cookie is valid for
   */
  public static void setAuthTokenCookieDomainPath(Cookie cookie, String path) {
    if (LC.zimbra_authtoken_cookie_domain.value().length() > 0) {
      cookie.setDomain(LC.zimbra_authtoken_cookie_domain.value());
    }

    cookie.setPath(path);
  }

  public static boolean secureCookie(HttpServletRequest request) {
    return "https".equalsIgnoreCase(request.getScheme());
  }

  public static void addHttpOnlyCookie(
      HttpServletResponse response,
      String name,
      String value,
      String path,
      Integer maxAge,
      boolean secure) {
    if (name.equalsIgnoreCase(COOKIE_ZM_AUTH_TOKEN)
        || name.equalsIgnoreCase(COOKIE_ZM_ADMIN_AUTH_TOKEN)) {
      addCookie(response, name, value, path, maxAge, true, true);
      SameSiteAttribute.addSameSiteAttribute(response, "Lax");
    } else {
      addCookie(response, name, value, path, maxAge, true, secure);
    }
  }

  private static void addCookie(
      HttpServletResponse response,
      String name,
      String value,
      String path,
      Integer maxAge,
      boolean httpOnly,
      boolean secure) {
    Cookie cookie = new Cookie(name, value);

    if (maxAge != null) {
      // jetty actually turns maxAge(lifetime in seconds) into an
      // Expires directive, not the Max-Age directive.
      cookie.setMaxAge(maxAge.intValue());
    }
    ZimbraCookie.setAuthTokenCookieDomainPath(cookie, ZimbraCookie.PATH_ROOT);

    cookie.setSecure(secure);

    if (httpOnly) {
      cookie.setHttpOnly(httpOnly);
    }
    response.addCookie(cookie);
  }

  public static void clearCookie(HttpServletResponse response, String cookieName) {
    Cookie cookie = new Cookie(cookieName, "");
    cookie.setMaxAge(0);
    setAuthTokenCookieDomainPath(cookie, PATH_ROOT);
    response.addCookie(cookie);
  }
}

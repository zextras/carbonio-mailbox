package com.zimbra.common.util;

import java.util.Collection;
import javax.servlet.http.HttpServletResponse;

/**
 * @since 4.0.9
 * @author keshavbhatt
 *     <p>Utility class to set SameSite attribute of the Set-Cookie HTTP response header. SameSite
 *     attribute allows us to declare if the cookie should be restricted to a first-party or
 *     same-site context
 */
public class SameSiteAttribute {

  /**
   * @param response the response object containing the Set-Cookies header
   * @param sameSiteValue value to be set for SameSite attribute
   */
  public static void addSameSiteAttribute(HttpServletResponse response, String sameSiteValue) {
    Collection<String> headers = response.getHeaders("Set-Cookie");
    boolean firstHeader = true;
    for (String header : headers) {
      if (firstHeader) {
        response.setHeader(
            "Set-Cookie", String.format("%s; %s", header, "SameSite=" + sameSiteValue));
        firstHeader = false;
        continue;
      }
      response.addHeader(
          "Set-Cookie", String.format("%s; %s", header, "SameSite=" + sameSiteValue));
    }
  }
}

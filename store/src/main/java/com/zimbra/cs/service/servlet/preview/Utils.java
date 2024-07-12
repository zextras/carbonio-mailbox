package com.zimbra.cs.service.servlet.preview;

import java.util.Arrays;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;

/** This Utility class contains common utility methods to be used by the Preview Servlet. */
class Utils {

  public static final String REQUEST_ID_ATTRIBUTE_KEY = "requestId";

  private Utils() {
    // Private constructor to prevent instantiation
  }

  /**
   * Constructs the full URL from the given HttpServletRequest.
   * This includes the request URL and the query string, if present.
   *
   * @param req the HttpServletRequest object from which to extract the full URL
   * @return the full URL as a String, including the query string if it exists
   * @throws IllegalArgumentException if the HttpServletRequest is null
   */
  static String getFullURLFromRequest(HttpServletRequest req) {
    if (req == null) {
      throw new IllegalArgumentException("HttpServletRequest cannot be null");
    }
    var requestURL = req.getRequestURL().toString();
    var queryString = req.getQueryString();
    return queryString == null ? requestURL : requestURL + "?" + queryString;
  }

  /**
   * Retrieves the request ID from the given HttpServletRequest.
   * The request ID is obtained from the request attribute specified by REQUEST_ID_ATTRIBUTE_KEY.
   *
   * @param req the HttpServletRequest object from which to retrieve the request ID
   * @return the request ID as a String
   * @throws IllegalArgumentException if the HttpServletRequest is null
   */
  static String getRequestIDFromRequest(HttpServletRequest req) {
    if (req == null) {
      throw new IllegalArgumentException("HttpServletRequest cannot be null");
    }
    return req.getAttribute(REQUEST_ID_ATTRIBUTE_KEY).toString();
  }

  /**
   * removes the disposition query parameter from the url
   *
   * @param requestUrl      the requestUrl ({@link String})
   * @param dispositionType disposition type ({@link String})
   * @return Request Url for Preview ({@link String})
   */
  static String getRequestUrlForPreview(String requestUrl, String dispositionType) {
    var dispQueryParam = "\\?disp=" + dispositionType;
    var possibleDisposition =
        Arrays.asList(dispQueryParam, "\\&disp=" + dispositionType);

    return possibleDisposition.stream()
        .reduce(
            requestUrl,
            (str, toRem) ->
                str.replaceAll(
                        toRem.contains("\\?") ? dispQueryParam + "&" : toRem,
                        toRem.contains("\\?") ? "\\?" : "")
                    .replaceAll(toRem.contains("\\?") ? dispQueryParam : toRem, ""));
  }

  /**
   * Returns the value of disposition type requested in URL's query parameter
   *
   * @param requestUrl the request URL
   * @return disposition value if found else the default "i"(inline)
   */
  static String getDispositionTypeFromQueryParams(String requestUrl) {
    if (requestUrl.split("\\?").length > 1) {
      return Stream.of(requestUrl.split("\\?")[1].split("&"))
          .map(kv -> kv.split("="))
          .filter(kv -> "disp".equalsIgnoreCase(kv[0]))
          .map(kv -> kv[1])
          .findFirst()
          .orElse("i");
    } else {
      return "i";
    }
  }


}

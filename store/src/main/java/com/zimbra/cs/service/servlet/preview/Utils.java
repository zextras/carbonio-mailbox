package com.zimbra.cs.service.servlet.preview;

import com.zextras.carbonio.preview.queries.BlobResponse;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;

/** This Utility class contains common utility methods to be used by the Preview Servlet. */
class Utils {

  public static final String REQUEST_ID_KEY = "tRequestId";
  public static final String REQUEST_PARAM_DISP = "disp";

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
   * The request ID is obtained first from the request attribute specified by {@link #REQUEST_ID_KEY} key.
   * If the attribute is not set then falls back to get the request ID from the request's query parameter.
   * If the query parameter do not contain query param with key {@link #REQUEST_ID_KEY} then returns null.
   *
   *
   * @param req the HttpServletRequest object from which to retrieve the request ID
   * @return the request ID as a String
   * @throws IllegalArgumentException if the HttpServletRequest is null
   */
  public static String getRequestIdFromRequest(HttpServletRequest req) {
    if (req == null) {
      throw new IllegalArgumentException("HttpServletRequest cannot be null");
    }

    var requestId = req.getAttribute(REQUEST_ID_KEY);
    if (requestId != null) {
      return requestId.toString();
    }

    var requestIdFromQuery = req.getParameter(REQUEST_ID_KEY);
    if (requestIdFromQuery != null && !requestIdFromQuery.isEmpty()) {
      return requestIdFromQuery;
    }

    return null;
  }

  public static String getOrSetRequestId(HttpServletRequest request) {
    var requestId = getRequestIdFromRequest(request);
    if (requestId == null || requestId.isEmpty()) {
      requestId = UUID.randomUUID().toString();
      request.setAttribute(Utils.REQUEST_ID_KEY, requestId);
    }
    return requestId;
  }

  public static String removeQueryParams(String requestUrl, List<String> paramsToRemove)
      {
        int queryIndex = requestUrl.indexOf('?');
        if (queryIndex == -1) {
          return requestUrl;
        }

        String baseUrl = requestUrl.substring(0, queryIndex);
        String query = requestUrl.substring(queryIndex + 1);

        List<String> queryPairs = Arrays.stream(query.split("&"))
            .filter(param -> {
              String paramName = param.contains("=") ? param.substring(0, param.indexOf('=')) : param;
              return !paramsToRemove.contains(paramName);
            })
            .collect(Collectors.toList());

        if (queryPairs.isEmpty()) {
          return baseUrl;
        }

        String newQuery = String.join("&", queryPairs);
        return baseUrl + "?" + newQuery;
  }

  /**
   * Returns the value of disposition type requested in URL's query parameter
   *
   * @param requestUrl the request URL
   * @return disposition value if found else the default "i"(inline)
   */
  static String getDispositionTypeFromQueryParams(String requestUrl) {
    if (requestUrl == null || requestUrl.isEmpty()) {
      return "i";
    }

    var parts = requestUrl.split("\\?");
    if (parts.length > 1) {
      return Stream.of(parts[1].split("&"))
          .map(kv -> kv.split("=", 2))
          .filter(kv -> REQUEST_PARAM_DISP.equalsIgnoreCase(kv[0]))
          .map(kv -> kv.length > 1 && !kv[1].isEmpty() ? kv[1] : "i")
          .findFirst()
          .orElse("i");
    } else {
      return "i";
    }
  }

  static ItemId getItemIdFromMessageId(ItemIdFactory itemIdFactory, String messageId, AuthToken authToken) throws ServiceException {
    final var uuidMsgId = messageId.split(":");
    if (uuidMsgId.length == 2) {
      return itemIdFactory.create(uuidMsgId[1], uuidMsgId[0]);
    }
    return itemIdFactory.create(messageId, authToken.getAccountId());
  }

  /**
   * This method is used to map the preview service's {@link BlobResponse} to our {@link BlobResponseStore} object
   *
   * @param response        preview service's {@link BlobResponse}
   * @param fileName        filename that we want to assign to our {@link BlobResponseStore} object
   * @param dispositionType disposition will be: attachment or inline(default)
   * @return mapped {@link BlobResponseStore} object
   */
  static Try<BlobResponseStore> mapResponseToBlobResponseStore(
      BlobResponse response, String fileName, String dispositionType) {
    return Try.of(() -> new BlobResponseStore(
        response.getContent(),
        fileName,
        response.getLength(),
        response.getMimeType(),
        dispositionType));
  }

}

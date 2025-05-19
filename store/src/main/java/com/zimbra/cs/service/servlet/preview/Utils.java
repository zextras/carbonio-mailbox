package com.zimbra.cs.service.servlet.preview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zextras.carbonio.preview.queries.Query;
import com.zextras.carbonio.preview.queries.Query.QueryBuilder;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.service.util.ItemId;
import io.vavr.control.Try;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.mail.MessagingException;
import javax.mail.internet.MimePart;
import javax.servlet.http.HttpServletRequest;

/**
 * This Utility class. Contains common utility/helper methods to be used by the Preview Servlet.
 */
class Utils {

  private Utils() {
    // Prevent instantiation
  }

  /**
   * Constructs the full URL from the given HttpServletRequest. This includes the request URL and the query string, if
   * present.
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
   * Retrieves the request ID from the given HttpServletRequest. The request ID is obtained first from the request
   * attribute specified by {@link Constants#REQUEST_ID_KEY} key. If the attribute is not set then falls back to get the
   * request ID from the request's query parameter. If the query parameter do not contain query param with key {@link
   * Constants#REQUEST_ID_KEY} then returns null.
   *
   * @param req the HttpServletRequest object from which to retrieve the request ID
   * @return the request ID as a String
   * @throws IllegalArgumentException if the HttpServletRequest is null
   */
  static String getRequestIdFromRequest(HttpServletRequest req) {
    if (req == null) {
      throw new IllegalArgumentException("HttpServletRequest cannot be null");
    }

    var requestId = req.getAttribute(Constants.REQUEST_ID_KEY);
    if (requestId != null) {
      return requestId.toString();
    }

    var requestIdFromQuery = req.getParameter(Constants.REQUEST_ID_KEY);
    if (requestIdFromQuery != null && !requestIdFromQuery.isEmpty()) {
      return requestIdFromQuery;
    }

    return null;
  }

  /**
   * Get request ID using {@link Utils#getRequestIdFromRequest(HttpServletRequest)} method if not found sets on using
   * {@code UUID.randomUUID().toString()}
   *
   * <p>The unique requestId is added to the request as attribute with key {@link Constants#REQUEST_ID_KEY}
   * Which can be retrieved using {@link  Utils#getRequestIdFromRequest(HttpServletRequest)}. * </p>
   *
   * @param request the {@link HttpServletRequest} from which the requestId has to be retrieved
   * @return requestId {@link String}
   */
  static String getOrSetRequestId(HttpServletRequest request) {
    var requestId = getRequestIdFromRequest(request);
    if (requestId == null || requestId.isEmpty()) {
      requestId = UUID.randomUUID().toString();
      request.setAttribute(Constants.REQUEST_ID_KEY, requestId);
    }
    return requestId;
  }

  /**
   * Removes given query parameters from the supplied requestUrl {@link String}
   *
   * @param requestUrl     URL from which query parameters are to be removed
   * @param paramsToRemove {@link List} of query parameters to be removed
   * @return cleaned up URL {@link String} without passed query parameters
   */
  static String removeQueryParams(String requestUrl, List<String> paramsToRemove) {
    var queryIndex = requestUrl.indexOf('?');
    if (queryIndex == -1) {
      return requestUrl;
    }

    var baseUrl = requestUrl.substring(0, queryIndex);
    var query = requestUrl.substring(queryIndex + 1);

    var queryPairs = Arrays.stream(query.split("&"))
        .filter(param -> {
          var paramName = param.contains("=") ? param.substring(0, param.indexOf('=')) : param;
          return !paramsToRemove.contains(paramName);
        })
        .collect(Collectors.toList());

    if (queryPairs.isEmpty()) {
      return baseUrl;
    }

    var newQuery = String.join("&", queryPairs);
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
          .filter(kv -> Constants.REQUEST_PARAM_DISP.equalsIgnoreCase(kv[0]))
          .map(kv -> kv.length > 1 && !kv[1].isEmpty() ? kv[1] : "i")
          .findFirst()
          .orElse("i");
    } else {
      return "i";
    }
  }

  /**
   * Returns {@link ItemId} object created using passed {@link ItemIdFactory} instance
   *
   * @param itemIdFactory the factory to create the {@link ItemId} object from {@code messageId} and {@code authToken}
   * @param messageId     the message id {@link String}
   * @param authToken     the {@link AuthToken}
   * @return the {@link ItemId} object created
   */
  static ItemId getItemIdFromMessageId(ItemIdFactory itemIdFactory, String messageId, AuthToken authToken)
      throws ServiceException {
    var uuidMsgId = messageId.split(":");
    if (uuidMsgId.length == 2) {
      var itemId = uuidMsgId[1];
      var accountId = uuidMsgId[0];
      return itemIdFactory.create(itemId, accountId);
    }
    return itemIdFactory.create(messageId, authToken.getAccountId());
  }

  /**
   * Maps preview service's {@link com.zextras.carbonio.preview.queries.BlobResponse} to {@link DataBlob}.
   *
   * @param response        preview service's {@link com.zextras.carbonio.preview.queries.BlobResponse}
   * @param fileName        filename that we want to assign to our {@link DataBlob} object
   * @param dispositionType disposition will be: attachment or inline(default)
   * @return mapped {@link DataBlob} object
   */
  static Try<DataBlob> mapPreviewResponseToDataBlob(
      com.zextras.carbonio.preview.queries.BlobResponse response, String fileName, String dispositionType) {
    return Try.of(() -> new DataBlob(
        response.getContent(),
        fileName,
        response.getLength(),
        response.getMimeType(),
        dispositionType));
  }

  /**
   * Maps a {@link MimePart} to a {@link DataBlob}.
   *
   * <p>This method converts a {@link MimePart} object into a {@link DataBlob} object
   * by extracting the input stream, file name, size, content type, and a fixed disposition value.</p>
   *
   * @param mimePart the {@link MimePart} to be mapped
   * @return a {@link DataBlob} containing the details extracted from the {@link MimePart}
   * @throws MessagingException if there is an error retrieving information from the {@link MimePart}
   * @throws IOException        if an I/O error occurs while accessing the input stream of the {@link MimePart}
   */
  static DataBlob mapMimePartResponseToDataBlob(MimePart mimePart) throws MessagingException, IOException {
    return new DataBlob(
        mimePart.getInputStream(),
        mimePart.getFileName(),
        (long) mimePart.getSize(),
        mimePart.getContentType(),
        "inline"
    );
  }

  /**
   * This method generates the final query {@link Query} from passed Optional area string and {@link
   * PreviewQueryParameters}
   *
   * @param optArea         the optional area {@link String} parameter
   * @param queryParameters the {@link PreviewQueryParameters} object
   * @return {@link Query}
   */
  static Query generateQuery(String optArea, PreviewQueryParameters queryParameters) {
    var parameterBuilder = new QueryBuilder();
    if (optArea != null) {
      parameterBuilder.setPreviewArea(optArea);
    }
    queryParameters.getQuality().ifPresent(parameterBuilder::setQuality);
    queryParameters.getOutputFormat().ifPresent(parameterBuilder::setOutputFormat);
    queryParameters.getCrop().ifPresent(parameterBuilder::setCrop);
    queryParameters.getShape().ifPresent(parameterBuilder::setShape);
    queryParameters.getFirstPage().ifPresent(parameterBuilder::setFirstPage);
    queryParameters.getLastPage().ifPresent(parameterBuilder::setLastPage);
    queryParameters.getLangTag().ifPresent(parameterBuilder::setLangTag);
    return parameterBuilder.build();
  }


  /**
   * This method parses the passed queryParamString into {@link PreviewQueryParameters} object
   *
   * @param queryParameters {@link String}
   * @return {@link PreviewQueryParameters}
   */
  static PreviewQueryParameters parseQueryParameters(String queryParameters) {
    var parameters =
        Arrays.stream(queryParameters.replace("?", "").split("&"))
            .map(parameter -> parameter.split("="))
            .filter(parameter -> parameter.length == 2)
            .collect(Collectors.toMap(parameter -> parameter[0], parameter -> parameter[1]));
    return new ObjectMapper().convertValue(parameters, PreviewQueryParameters.class);
  }

  /**
   * Extracts and validates the required query parameters from the given HTTP request URL.
   *
   * <p>This static method uses a regular expression pattern to match and extract the necessary
   * query parameters from the full URL of the provided {@link HttpServletRequest} object. It expects the URL to contain
   * exactly three groups as defined by the pattern. If successful, it returns the extracted parameters in a {@link Try}
   * object. If the parameters are missing or do not match the expected format, it returns a {@link Try} failure with an
   * appropriate exception.</p>
   *
   * @param request the {@link HttpServletRequest} object containing the request details
   * @return a {@link Try} containing an array of strings with the extracted query parameters, or a failure if the
   * parameters are invalid
   * @throws IllegalArgumentException if the query parameters do not match the expected pattern
   */
  static Try<String[]> extractRequiredQueryParameters(HttpServletRequest request) {
    var matcher = Constants.REQUIRED_QUERY_PARAMETERS_PATTERN.matcher(getFullURLFromRequest(request));
    if (matcher.find() && matcher.groupCount() == 3) {
      return Try.success(new String[]{matcher.group(2), matcher.group(3)});
    } else {
      return Try.failure(new IllegalArgumentException("Invalid query parameters"));
    }
  }


  static RuntimeException remapAccountRelatedException(Throwable ex) {
    var message = ex.getMessage();
    if (message == null || message.trim().isEmpty() || message.toLowerCase().contains("account")) {
      message = "Error processing requested attachment. Ensure message ID or account are correct.";
    }
    return new RuntimeException(message, ex);
  }

}

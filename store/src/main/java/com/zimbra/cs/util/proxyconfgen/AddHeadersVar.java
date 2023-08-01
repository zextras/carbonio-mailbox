package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.service.ServiceException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddHeadersVar extends ProxyConfVar {

  private final ArrayList<String> responseHeaders;
  private final Map<String, String> customLoginLogoutUrls;

  public AddHeadersVar(
      String key,
      ArrayList<String> responseHeaders,
      String description,
      Map<String, String> customLoginLogoutUrls) {
    super(key, null, null, ProxyConfValueType.CUSTOM, ProxyConfOverride.CUSTOM, description);
    this.responseHeaders = responseHeaders;
    this.customLoginLogoutUrls = customLoginLogoutUrls;
  }

  @Override
  public void update() throws ServiceException {
    ArrayList<KeyValue> directives = new ArrayList<>();

    for (String headerLine : responseHeaders) {
      KeyValue header = parseHeaderLine(headerLine);

      // Handle CSP header
      if (header.key.equalsIgnoreCase("Content-Security-Policy")) {
        String newCspValue = modifyCspHeaderValue(header.value);
        if (!newCspValue.isEmpty()) {
          header = new KeyValue(header.key, newCspValue);
        }
      }
      directives.add(header);
    }

    mValue = directives;
  }

  /**
   * Parses a header line and extracts the key-value pair.
   *
   * @param headerLine The header line to parse.
   * @return A KeyValue object representing the key-value pair of the header.
   * @author Keshav Bhatt
   * @since 23.9.0
   */
  KeyValue parseHeaderLine(String headerLine) {
    final Matcher matcher = RE_HEADER.matcher(headerLine);
    if (matcher.matches()) {
      return new KeyValue(matcher.group(1), matcher.group(2));
    } else {
      return new KeyValue(headerLine);
    }
  }

  /**
   * Modifies the Content-Security-Policy header value by adding custom URLs to the connect-src
   * directive if they don't exist. If no connect-src directive is found, the original value is
   * returned.
   *
   * @param cspValue The original Content-Security-Policy header value to modify.
   * @return The modified Content-Security-Policy header value with added custom URLs to the
   *     connect-src directive, or an empty string if the connect-src directive is missing.
   * @author Keshav Bhatt
   * @since 23.9.0
   */
  String modifyCspHeaderValue(String cspValue) {
    final String connectSrcDirective = extractConnectSrcDirective(cspValue);
    if (connectSrcDirective.isEmpty()) {
      // Skip updating if no connect-src directive is found
      return "";
    }

    if (customLoginLogoutUrls == null) {
      // Return the original CSP header value if customLoginLogoutUrls is null
      return cspValue;
    }

    // Add customLoginLogoutUrls if they do not exist in connect-src directive
    final Set<String> addedUrls = new HashSet<>();
    final StringBuilder newConnectSrcDirectiveBuilder = new StringBuilder(connectSrcDirective);

    for (String url : customLoginLogoutUrls.values()) {
      if (isValidSrcDirectiveUrl(url)
          && (!addedUrls.contains(url.toLowerCase())
              && !connectSrcDirective.toLowerCase().contains(url.toLowerCase()))) {
        newConnectSrcDirectiveBuilder.append(" ").append(url);
        addedUrls.add(url.toLowerCase());
      }
    }

    // Prepare the new connectSrcDirective as needed
    final String newConnectSrcDirective = newConnectSrcDirectiveBuilder.toString();
    if (!connectSrcDirective.equalsIgnoreCase(newConnectSrcDirective)) {
      return cspValue.replace(connectSrcDirective, newConnectSrcDirective);
    }

    return "";
  }

  /**
   * Extracts the connect-src directive from the Content-Security-Policy header. The method uses a
   * case-insensitive regular expression pattern to match the directive.
   *
   * @param cspHeader The Content-Security-Policy header to extract the connect-src directive from.
   * @return The connect-src directive of the Content-Security-Policy header if found, or an empty
   *     string if not found.
   * @author Keshav Bhatt
   * @since 23.9.0
   */
  String extractConnectSrcDirective(String cspHeader) {
    final Pattern pattern = Pattern.compile("connect-src[^;]*", Pattern.CASE_INSENSITIVE);
    final Matcher matcher = pattern.matcher(cspHeader);
    if (matcher.find()) {
      return matcher.group().trim();
    } else {
      return "";
    }
  }

  /**
   * Checks if the provided URL is a valid source directive URL that can be used in a
   * Content-Security-Policy.
   *
   * @param url The URL to be validated.
   * @return {@code true} if the URL is a valid source directive URL, otherwise {@code false}.
   * @author Keshav Bhatt
   * @since 23.9.0
   */
  static boolean isValidSrcDirectiveUrl(String url) {
    return Pattern.matches(
        "^(https?://(w{3}\\.)?)?((\\*\\.)?\\w+(-\\w+)*\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\*|/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?)?$",
        url);
  }

  @Override
  public String format(Object o) {
    @SuppressWarnings("unchecked")
    ArrayList<KeyValue> headers = (ArrayList<KeyValue>) o;
    StringBuilder sb = new StringBuilder();
    for (KeyValue header : headers) {
      mLog.debug("Adding directive add_header " + header.key + " " + header.value);
      if (sb.length() > 0) {
        sb.append(System.lineSeparator()).append("    ");
      }
      sb.append(String.format("add_header %s %s;", header.key, header.value));
    }
    return sb.toString();
  }
}

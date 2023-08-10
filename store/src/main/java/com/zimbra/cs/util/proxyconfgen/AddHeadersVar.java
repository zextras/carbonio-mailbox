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
      KeyValue header = ProxyConfUtil.parseHeaderLine(headerLine);

      // Handle CSP header
      if (header.key.equalsIgnoreCase("Content-Security-Policy")) {
        String newCspValue = generateModifiedCspHeaderValue(header.value);
        if (!newCspValue.isEmpty()) {
          header = new KeyValue(header.key, newCspValue);
        }
      }
      directives.add(header);
    }

    mValue = directives;
  }

  /**
   * Generates a modified Content-Security-Policy header value by adding custom URLs to the
   * connect-src directive if they don't exist. If no connect-src directive is found, an empty
   * string is returned.
   *
   * @param cspValue The original Content-Security-Policy header value to modify.
   * @return The modified Content-Security-Policy header value with added custom URLs to the
   *     connect-src directive, or an empty string if the connect-src directive is missing.
   * @author Keshav Bhatt
   * @since 23.9.0
   */
  String generateModifiedCspHeaderValue(String cspValue) {
    final String connectSrcDirective = extractConnectSrcDirective(cspValue);
    if (connectSrcDirective.isEmpty()) {
      return "";
    }

    if (customLoginLogoutUrls == null) {
      return cspValue;
    }

    final String newConnectSrcDirective =
        generateModifiedConnectSrcDirectiveWithCustomUrls(connectSrcDirective);
    if (!connectSrcDirective.equalsIgnoreCase(newConnectSrcDirective)) {
      return cspValue.replace(connectSrcDirective, newConnectSrcDirective);
    }

    return "";
  }

  /**
   * Adds custom URLs to the connect-src directive of a Content-Security-Policy header value, if
   * they don't already exist, and returns a String containing the modified directive.
   *
   * @param connectSrcDirective The original connect-src directive extracted from the CSP header
   *     value.
   * @return A String containing the modified connect-src directive with added custom URLs.
   * @since 23.9.0
   */
  private String generateModifiedConnectSrcDirectiveWithCustomUrls(String connectSrcDirective) {
    final Set<String> addedUrls = new HashSet<>();
    final StringBuilder newConnectSrcDirectiveBuilder = new StringBuilder(connectSrcDirective);

    for (String url : customLoginLogoutUrls.values()) {
      if (ProxyConfUtil.isValidSrcDirectiveUrl(url)
          && (!addedUrls.contains(url.toLowerCase())
              && !connectSrcDirective.toLowerCase().contains(url.toLowerCase()))) {
        newConnectSrcDirectiveBuilder.append(" ").append(url);
        addedUrls.add(url.toLowerCase());
      }
    }
    return newConnectSrcDirectiveBuilder.toString();
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

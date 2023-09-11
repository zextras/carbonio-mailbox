package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents a {@link ProxyConfVar} that is used to provide response headers for the
 * proxy.
 */
public class AddHeadersVar extends ProxyConfVar {

  private final ArrayList<String> responseHeaders;
  private final List<String> customLoginLogoutUrls;

  /**
   * Creates a new instance of the {@link AddHeadersVar} class. that represents <code>add_header
   * </code> nginx directive.
   *
   * @param keyword Unique keyword for this variable that will be used to explode the value in nginx
   *     templates.
   * @param responseHeaders List of custom response headers.
   * @param description Description of this configuration variable.
   * @param customLoginLogoutUrls List of custom login/logout URLs.
   */
  public AddHeadersVar(
      Provisioning prov,
      String keyword,
      ArrayList<String> responseHeaders,
      String description,
      List<String> customLoginLogoutUrls) {
    super(
        prov,
        keyword,
        null,
        null,
        ProxyConfValueType.CUSTOM,
        ProxyConfOverride.CUSTOM,
        description);
    this.responseHeaders = responseHeaders;
    this.customLoginLogoutUrls = customLoginLogoutUrls;
  }

  /**
   * Overrides the update method to process and modify response headers.
   *
   * @throws ServiceException If there is an issue during the header processing.
   */
  @Override
  public void update() throws ServiceException {
    ArrayList<KeyValue> directives = new ArrayList<>();

    for (String headerLine : responseHeaders) {
      KeyValue header = ProxyConfUtil.parseHeaderLine(headerLine);

      if (header.key.equalsIgnoreCase("Content-Security-Policy")) {
        final String newCspValue = generateModifiedCspHeaderValue(header.value);
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
  private String generateModifiedCspHeaderValue(String cspValue) {
    final String connectSrcDirective = ProxyConfUtil.extractConnectSrcDirectiveFromCSP(cspValue);
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

    for (String url : customLoginLogoutUrls) {
      if (ProxyConfUtil.isValidSrcDirectiveUrl(url)
          && (!addedUrls.contains(url.toLowerCase())
              && !connectSrcDirective.toLowerCase().contains(url.toLowerCase()))) {
        newConnectSrcDirectiveBuilder.append(" ").append(url);
        addedUrls.add(url.toLowerCase());
      }
    }
    return newConnectSrcDirectiveBuilder.toString();
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

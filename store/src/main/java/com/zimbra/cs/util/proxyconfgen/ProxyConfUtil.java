package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.util.proxyconfgen.ProxyConfVar.KeyValue;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProxyConfUtil {

  private ProxyConfUtil() {}

  public static void writeContentToFile(String content, String filePath) throws ServiceException {

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
      bw.write(content);
    } catch (IOException e) {
      throw ServiceException.FAILURE(
          "Cannot write the content (" + content + ") to " + filePath, e);
    }
  }

  public static boolean isEmptyString(String target) {
    return (target == null) || (target.trim().equalsIgnoreCase(""));
  }

  public static InetAddress getLookupTargetIPbyIPMode(String hostname) throws ProxyConfException {

    InetAddress[] ips;
    try {
      ips = InetAddress.getAllByName(hostname);
    } catch (UnknownHostException e) {
      throw new ProxyConfException("the lookup target " + hostname + " can't be resolved");
    }
    IPModeEnablerVar.IPMode mode = IPModeEnablerVar.getZimbraIPMode();

    if (mode == IPModeEnablerVar.IPMode.IPV4_ONLY) {
      for (InetAddress ip : ips) {
        if (ip instanceof Inet4Address) {
          return ip;
        }
      }
      throw new ProxyConfException(
          "Can't find valid lookup target IPv4 address when zimbra IP mode is IPv4 only");
    } else if (mode == IPModeEnablerVar.IPMode.IPV6_ONLY) {
      for (InetAddress ip : ips) {
        if (ip instanceof Inet6Address) {
          return ip;
        }
      }
      throw new ProxyConfException(
          "Can't find valid lookup target IPv6 address when zimbra IP mode is IPv6 only");
    } else {
      for (InetAddress ip : ips) {
        if (ip instanceof Inet4Address) {
          return ip;
        }
      }
      return ips[0]; // try to return an IPv4, but if there is none,
      // simply return the first IPv6
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

  /**
   * Parses a header line and extracts the key-value pair.
   *
   * @param headerLine The header line to parse.
   * @return A KeyValue object representing the key-value pair of the header.
   * @author Keshav Bhatt
   * @since 23.9.0
   */
  static KeyValue parseHeaderLine(String headerLine) {
    final Matcher matcher = ProxyConfVar.RE_HEADER.matcher(headerLine);
    if (matcher.matches()) {
      return new KeyValue(matcher.group(1), matcher.group(2));
    } else {
      return new KeyValue(headerLine);
    }
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
  static String extractConnectSrcDirectiveFromCSP(String cspHeader) {
    final Pattern pattern = Pattern.compile("connect-src[^;]*", Pattern.CASE_INSENSITIVE);
    final Matcher matcher = pattern.matcher(cspHeader);
    if (matcher.find()) {
      return matcher.group().trim();
    } else {
      return "";
    }
  }
}

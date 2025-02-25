package com.zimbra.cs.servlet;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class RequestMetadataUtil {

  private final boolean includeMethod;
  private final boolean includeUrl;
  private final boolean includeQueryString;
  private final boolean includeRemoteIp;
  private final boolean includeHeaders;
  private final boolean includeCookies;
  private final boolean includeParameters;
  private final boolean includeSession;

  private RequestMetadataUtil(RequestMetadataAsStringBuilder requestMetadataAsStringBuilder) {
    this.includeMethod = requestMetadataAsStringBuilder.includeMethod;
    this.includeUrl = requestMetadataAsStringBuilder.includeUrl;
    this.includeQueryString = requestMetadataAsStringBuilder.includeQueryString;
    this.includeRemoteIp = requestMetadataAsStringBuilder.includeRemoteIp;
    this.includeHeaders = requestMetadataAsStringBuilder.includeHeaders;
    this.includeCookies = requestMetadataAsStringBuilder.includeCookies;
    this.includeParameters = requestMetadataAsStringBuilder.includeParameters;
    this.includeSession = requestMetadataAsStringBuilder.includeSession;
  }

  /**
   * Extracts and formats request metadata as a string based on the configured options.
   *
   * @param request The HttpServletRequest object.
   * @return A string containing the selected request metadata.
   */
  private String getRequestMetadataAsString(HttpServletRequest request) {
    Objects.requireNonNull(request, "Request cannot be null");

    StringBuilder metadata = new StringBuilder();

    if (includeMethod) {
      metadata.append("Request Method: ").append(request.getMethod()).append("\n");
    }

    if (includeUrl) {
      metadata.append("Request URL: ").append(request.getRequestURL().toString()).append("\n");
    }

    if (includeQueryString) {
      String queryString = request.getQueryString();
      if (queryString != null) {
        metadata.append("Query String: ").append(queryString).append("\n");
      }
    }

    if (includeRemoteIp) {
      metadata.append("Remote IP: ").append(request.getRemoteAddr()).append("\n");
    }

    if (includeHeaders) {
      metadata.append("Headers:\n");
      Enumeration<String> headerNames = request.getHeaderNames();
      if (headerNames != null) {
        while (headerNames.hasMoreElements()) {
          String headerName = headerNames.nextElement();
          if (isSensitiveHeader(headerName)) {
            continue;
          }
          String headerValue = request.getHeader(headerName);
          metadata.append("  ").append(headerName).append(": ").append(headerValue).append("\n");
        }
      } else {
        metadata.append("  No headers\n");
      }
    }

    if (includeCookies) {
      metadata.append("Cookies:\n");
      javax.servlet.http.Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (javax.servlet.http.Cookie cookie : cookies) {
          metadata.append("  ").append(cookie.getName()).append(": ").append(cookie.getValue()).append("\n");
        }
      } else {
        metadata.append("  No cookies\n");
      }
    }

    if (includeParameters) {
      metadata.append("Parameters:\n");
      Map<String, String[]> parameters = request.getParameterMap();
      for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
        String paramName = entry.getKey();
        String[] paramValues = entry.getValue();
        metadata.append("  ").append(paramName).append(": ");
        if (paramValues.length == 1) {
          metadata.append(paramValues[0]);
        } else {
          metadata.append(Arrays.toString(paramValues));
        }
        metadata.append("\n");
      }
    }

    if (includeSession) {
      metadata.append("Session Information:\n");
      HttpSession session = request.getSession(false);
      if (session != null) {
        metadata.append("  Session ID: ").append(session.getId()).append("\n");
        Enumeration<String> sessionAttributeNames = session.getAttributeNames();
        while (sessionAttributeNames.hasMoreElements()) {
          String attributeName = sessionAttributeNames.nextElement();
          Object attributeValue = session.getAttribute(attributeName);
          metadata.append("  ").append(attributeName).append(": ").append(attributeValue).append("\n");
        }
      } else {
        metadata.append("  No session\n");
      }
    }

    return metadata.toString();
  }

  private boolean isSensitiveHeader(String headerName) {
    return "authorization".equalsIgnoreCase(headerName)
        || "proxy-authorization".equalsIgnoreCase(headerName)
        || "cookie".equalsIgnoreCase(headerName)
        || "x-forwarded-for".equalsIgnoreCase(headerName);
  }

  /**
   * Builder for RequestMetadataUtil.
   */
  @SuppressWarnings("unused")
  public static class RequestMetadataAsStringBuilder {

    private final HttpServletRequest request;

    private boolean includeMethod = false;
    private boolean includeUrl = false;
    private boolean includeQueryString = false;
    private boolean includeRemoteIp = false;
    private boolean includeHeaders = false;
    private boolean includeCookies = false;
    private boolean includeParameters = false;
    private boolean includeSession = false;

    /**
     * Creates a new Builder instance.
     *
     * @param request The HttpServletRequest object. Must not be null.
     * @throws NullPointerException If the request is null.
     */
    public RequestMetadataAsStringBuilder(HttpServletRequest request) {
      this.request = Objects.requireNonNull(request, "Request cannot be null");
    }

    public RequestMetadataAsStringBuilder withMethod() {
      this.includeMethod = true;
      return this;
    }

    public RequestMetadataAsStringBuilder withUrl() {
      this.includeUrl = true;
      return this;
    }

    public RequestMetadataAsStringBuilder withQueryString() {
      this.includeQueryString = true;
      return this;
    }

    public RequestMetadataAsStringBuilder withRemoteIp() {
      this.includeRemoteIp = true;
      return this;
    }

    public RequestMetadataAsStringBuilder withHeaders() {
      this.includeHeaders = true;
      return this;
    }

    public RequestMetadataAsStringBuilder withCookies() {
      this.includeCookies = true;
      return this;
    }

    public RequestMetadataAsStringBuilder withParameters() {
      this.includeParameters = true;
      return this;
    }

    public RequestMetadataAsStringBuilder withSession() {
      this.includeSession = true;
      return this;
    }

    /**
     * Builds the request metadata string based on the configured options.
     *
     * @return A string containing the selected request metadata.
     */
    public String build() {
      RequestMetadataUtil requestMetadataUtil = new RequestMetadataUtil(this);
      return requestMetadataUtil.getRequestMetadataAsString(request);
    }
  }
}
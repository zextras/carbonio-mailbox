package com.zimbra.cs.service.servlet.preauth;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.auth.AuthContext;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.AuthProviderException;
import com.zimbra.cs.servlet.ZimbraServlet;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

/** This Utility class contains common utility methods to be used by the PreAuth service. */
class Utils {

  private Utils() {
    // Private constructor to prevent instantiation
  }

  /**
   * Retrieves the value of the specified required parameter from the given HttpServletRequest.
   *
   * <p>If the parameter is present in the request, its value is returned. If the parameter is
   * missing, a ServiceException is thrown with an error message indicating that the parameter is
   * required.
   *
   * @param req The HttpServletRequest from which to retrieve the parameter.
   * @param paramName The name of the required parameter to retrieve.
   * @return The value of the required parameter if present.
   * @throws ServiceException If the required parameter is missing.
   * @see ServiceException
   */
  static String getRequiredParam(HttpServletRequest req, String paramName) throws ServiceException {
    String param = req.getParameter(paramName);
    if (param == null) {
      throw ServiceException.INVALID_REQUEST("missing required param: " + paramName, null);
    } else {
      return param;
    }
  }

  /**
   * Retrieves the value of an optional parameter from the provided {@link HttpServletRequest}. If
   * the parameter with the given name is present in the request, its value is returned as a
   * non-empty {@link Optional}. If the parameter is not present, the default value is returned as a
   * non-empty {@link Optional}. If the default value is null, the method returns an empty {@link
   * Optional}.
   *
   * @param req The {@link HttpServletRequest} object from which to retrieve the parameter.
   * @param paramName The name of the parameter to retrieve.
   * @param defaultValue The default value to return if the parameter is not present. If null, an
   *     empty {@link Optional} is returned.
   * @return A {@link Optional} containing the value of the parameter if present, or the default
   *     value if not present (or empty if defaultValue is null).
   */
  static Optional<String> getOptionalParam(
      HttpServletRequest req, String paramName, String defaultValue) {
    final String param = req.getParameter(paramName);
    if (param == null) {
      return Optional.ofNullable(defaultValue);
    } else {
      return Optional.of(param);
    }
  }

  /**
   * Retrieves the base URL of the given HttpServletRequest.
   *
   * <p>The base URL is constructed by combining the scheme (http/https) and the server name from
   * the request.
   *
   * @param request The HttpServletRequest from which to retrieve the base URL.
   * @return The base URL of the request (e.g., "http://example.com").
   */
  static String getBaseUrl(HttpServletRequest request) {
    String scheme = request.getScheme();
    String host = request.getServerName();

    return scheme + "://" + host;
  }

  /**
   * Sanitize the provided redirect URL by removing the protocol, host, and port information,
   * ensuring it is relative to the current context.
   *
   * @param redirectURL The original redirect URL to sanitize.
   * @return The sanitized redirect URL, or null if the URL is malformed.
   */
  static String convertRedirectURLRelativeToContext(String redirectURL)
      throws MalformedURLException {
    if (redirectURL == null) {
      return null;
    }

    String sanitizedURL = redirectURL;

    final URL url = new URL(redirectURL);
    final String protocol = url.getProtocol();
    final String host = url.getHost();
    final int port = url.getPort();

    final String protocolPattern = "^(http|https|ftp|file)://.*$";
    if (redirectURL.matches(protocolPattern)) {
      final String replaceProtocol = String.format("%s://", protocol);
      sanitizedURL = sanitizedURL.replace(replaceProtocol, "");
    }

    if (host != null) {
      String strToReplace;
      if (port == -1) {
        strToReplace = host;
      } else {
        strToReplace = String.format("%s:%d", host, port);
      }
      sanitizedURL = sanitizedURL.replace(strToReplace, "");
    }

    return sanitizedURL;
  }

  /**
   * Generates an authentication token (AuthToken) for the specified Account with optional
   * expiration and admin access.
   *
   * <p>The method generates an AuthToken based on the provided Account, expiration time (in
   * milliseconds), and an indicator for admin access. If admin is true, an admin AuthToken is
   * generated; otherwise, a regular AuthToken is created.
   *
   * @param acct The Account for which the AuthToken will be generated.
   * @param expires The expiration time of the AuthToken in milliseconds. Pass 0 for no expiration.
   * @param admin True if an admin AuthToken should be generated; false for a regular AuthToken.
   * @return The generated AuthToken.
   * @throws AuthProviderException If an error occurs while generating the AuthToken.
   * @see AuthToken
   * @see AuthProvider
   */
  static AuthToken generateAuthToken(Account acct, long expires, boolean admin)
      throws AuthProviderException {
    AuthToken authToken;
    if (admin) {
      if (expires == 0) {
        authToken = AuthProvider.getAuthToken(acct, true);
      } else {
        authToken = AuthProvider.getAuthToken(acct, expires, true, null);
      }
    } else {
      if (expires == 0) {
        authToken = AuthProvider.getAuthToken(acct);
      } else {
        authToken = AuthProvider.getAuthToken(acct, expires);
      }
    }
    return authToken;
  }

  /**
   * Creates an authentication context map for the given account identifier and HttpServletRequest.
   *
   * <p>The authentication context map contains information related to the authentication process,
   * such as originating client IP, remote IP, account name passed in, and user agent.
   *
   * @param accountIdentifier The account identifier used for authentication.
   * @param req The HttpServletRequest containing the client's request information.
   * @return A Map representing the authentication context with the following keys: - {@link
   *     AuthContext#AC_ORIGINATING_CLIENT_IP}: The originating client IP address. - {@link
   *     AuthContext#AC_REMOTE_IP}: The remote IP address of the client. - {@link
   *     AuthContext#AC_ACCOUNT_NAME_PASSEDIN}: The account name passed in for authentication. -
   *     {@link AuthContext#AC_USER_AGENT}: The user agent from the client's request headers.
   */
  static Map<String, Object> createAuthContext(String accountIdentifier, HttpServletRequest req) {
    Map<String, Object> authCtxt = new HashMap<>();
    authCtxt.put(AuthContext.AC_ORIGINATING_CLIENT_IP, ZimbraServlet.getOrigIp(req));
    authCtxt.put(AuthContext.AC_REMOTE_IP, ZimbraServlet.getClientIp(req));
    authCtxt.put(AuthContext.AC_ACCOUNT_NAME_PASSEDIN, accountIdentifier);
    authCtxt.put(AuthContext.AC_USER_AGENT, req.getHeader("User-Agent"));
    return authCtxt;
  }
}

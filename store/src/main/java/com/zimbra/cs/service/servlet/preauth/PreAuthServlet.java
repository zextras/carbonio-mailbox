// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Dec 20, 2004
 * @author Greg Solovyev
 * */
package com.zimbra.cs.service.servlet.preauth;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.account.ZAttrProvisioning.AutoProvAuthMech;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AccountServiceException.AuthFailedServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.account.names.NameUtil.EmailAddress;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.servlet.preauth.AutoProvisioningParams.AutoProvisioningParamsBuilder;
import com.zimbra.cs.servlet.ZimbraServlet;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PreAuthServlet extends ZimbraServlet {

  static final String DEFAULT_MAIL_URL = "/";
  static final String DEFAULT_ADMIN_URL = "/carbonioAdmin";

  @Override
  public void init() throws ServletException {
    String name = getServletName();
    ZimbraLog.account.info("Servlet " + name + " starting up");
    super.init();
  }

  @Override
  public void destroy() {
    String name = getServletName();
    ZimbraLog.account.info("Servlet " + name + " shutting down");
    super.destroy();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      ZimbraLog.clearContext();

      Provisioning provisioning = Provisioning.getInstance();
      Server server = provisioning.getLocalServer();
      String referMode = server.getAttr(ZAttrProvisioning.A_zimbraMailReferMode, "wronghost");

      boolean isRedirectParam =
          Utils.getOptionalParam(req, PreAuthParams.PARAM_IS_REDIRECT.getParamName(), "0")
              .equals("1");
      String rawAuthTokenParam =
          Utils.getOptionalParam(req, PreAuthParams.PARAM_AUTHTOKEN.getParamName(), null);

      if (rawAuthTokenParam != null) {
        handleRawAuthTokenRequest(
            req, resp, provisioning, rawAuthTokenParam, referMode, isRedirectParam);
      } else {
        handlePreAuthRequest(req, resp, provisioning, referMode, isRedirectParam);
      }
    } catch (ServiceException | AuthTokenException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (IOException ex) {
      resp.sendError(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "An error occurred while processing the request.");
    }
  }

  /**
   * Handles the processing of requests containing a raw authentication token passed as request
   * parameter.
   *
   * <p>This method is responsible for validating the provided raw authentication token by
   * converting it into an AuthToken instance using the AuthProvider. It then determines the
   * appropriate action based on the type of the AuthToken and whether a referral to the correct
   * server is needed. If the AuthToken indicates an admin account or requires referral, a new
   * AuthToken may be generated and sent back as a redirect. Otherwise, the method redirects the
   * request to the correct server using the existing AuthToken.
   *
   * @param req The HttpServletRequest object representing the incoming HTTP request.
   * @param resp The HttpServletResponse object representing the HTTP response to be sent back.
   * @param provisioning The Provisioning instance for retrieving account and server information.
   * @param rawAuthToken The raw authentication token obtained from the request.
   * @param referMode The mode for server referral, specifying whether redirection is needed or not.
   * @param isRedirect A boolean flag indicating if the request is a redirect request.
   * @throws AuthTokenException If there is an error while processing the authentication token.
   * @throws IOException If an I/O error occurs while handling the request.
   * @see AuthToken
   * @see AuthProvider
   */
  void handleRawAuthTokenRequest(
      HttpServletRequest req,
      HttpServletResponse resp,
      Provisioning provisioning,
      String rawAuthToken,
      String referMode,
      boolean isRedirect)
      throws AuthTokenException, IOException, ServiceException, ServletException {
    AuthToken authToken = AuthProvider.getAuthToken(rawAuthToken);
    validateAuthToken(authToken);

    boolean isAdmin = AuthToken.isAnyAdmin(authToken);
    Account acct = provisioning.get(AccountBy.id, authToken.getAccountId(), authToken);

    // We've got an auth token in the request,
    // Check if we need a redirect to the correct server
    if (isAdmin || needReferral(acct, referMode, isRedirect)) {
      // Authtoken in get request is for one time use only.
      // Deregister and generate new one.
      AuthToken updatedAuthToken = regenerateAuthToken(authToken);
      // No need to redirect to the correct server,
      // just send them off to do business
      setCookieAndRedirect(req, resp, updatedAuthToken);
    } else {
      // Redirect to the correct server with the incoming auth token
      // we no longer send the auth token we generate over when we
      // redirect to the correct server,
      // but customer can be sending a token in their preauth URL,
      // in this case, just send over the auth token as is.
      redirectToCorrectServer(req, resp, acct, null);
    }
  }

  /**
   * Handles the processing of requests containing preauth token passed as request parameter.
   *
   * <p>This method is responsible for processing the pre-authentication request. It extracts the
   * preauth parameters from the HttpServletRequest, such as the pre-authentication token, account
   * identifier, accountBy type, timestamp, expiration time, and admin flag. The method then
   * validates the account's status and admin access. If the request requires referral to the
   * correct server or if the admin flag is set, a new AuthToken may be generated based on the
   * account information, and the response will be sent as a redirect to the correct server.
   * Otherwise, the method redirects the request to the correct server using the existing AuthToken,
   * if available.
   *
   * @param req The HttpServletRequest object representing the incoming HTTP request.
   * @param resp The HttpServletResponse object representing the HTTP response to be sent back.
   * @param provisioning The Provisioning instance for retrieving account information.
   * @param referMode The mode for server referral, specifying whether redirection is needed or not.
   * @param isRedirect A boolean flag indicating if the request is a redirect request.
   * @throws IOException If an I/O error occurs while handling the request.
   * @throws AccountServiceException If there is an error while handling the Account service.
   * @see AuthToken
   * @see Provisioning
   */
  void handlePreAuthRequest(
      HttpServletRequest req,
      HttpServletResponse resp,
      Provisioning provisioning,
      String referMode,
      boolean isRedirect)
      throws IOException, ServiceException {

    String preAuthParam = Utils.getRequiredParam(req, PreAuthParams.PARAM_PRE_AUTH.getParamName());
    String accountParam = Utils.getRequiredParam(req, PreAuthParams.PARAM_ACCOUNT.getParamName());
    final boolean adminParam =
        Utils.getOptionalParam(req, PreAuthParams.PARAM_ADMIN.getParamName(), "0").equals("1")
            && isAdminRequest(req);
    final long timestampParam =
        Long.parseLong(Utils.getRequiredParam(req, PreAuthParams.PARAM_TIMESTAMP.getParamName()));
    final long expiresParam =
        Long.parseLong(Utils.getRequiredParam(req, PreAuthParams.PARAM_EXPIRES.getParamName()));
    final String accountByParam =
        Utils.getOptionalParam(req, PreAuthParams.PARAM_BY.getParamName(), AccountBy.name.name());
    final AccountBy accountBy = AccountBy.fromString(accountByParam);

    boolean accountAutoProvisioned = false;
    Account acct = provisioning.get(accountBy, accountParam, null);
    final Map<String, Object> authContext = Utils.createAuthContext(accountParam, req);

    if (acct == null) {
      acct =
          createAutoProvisionedAccount(
              new AutoProvisioningParamsBuilder(
                      accountParam,
                      accountBy,
                      provisioning,
                      adminParam,
                      timestampParam,
                      expiresParam,
                      preAuthParam)
                  .authContext(authContext)
                  .build());
      if (acct != null) {
        accountAutoProvisioned = true;
      } else {
        throw AuthFailedServiceException.AUTH_FAILED(
            accountParam, accountParam, "Account not found");
      }
    }

    if (!Provisioning.ACCOUNT_STATUS_ACTIVE.equalsIgnoreCase(acct.getAccountStatus(provisioning))) {
      handleInactiveAccount(acct, provisioning);
    }

    if (adminParam) {
      validateAdminAccount(acct);
    }

    if (adminParam || needReferral(acct, referMode, isRedirect)) {
      if (!accountAutoProvisioned) {
        provisioning.preAuthAccount(
            acct,
            accountParam,
            accountByParam,
            timestampParam,
            expiresParam,
            preAuthParam,
            adminParam,
            authContext);
      }
      AuthToken localAuthToken = Utils.generateAuthToken(acct, expiresParam, adminParam);
      setCookieAndRedirect(req, resp, localAuthToken);
    } else {
      redirectToCorrectServer(req, resp, acct, null);
    }
  }

  /**
   * Handles the validation of an admin account.
   *
   * <p>Checks if the provided account is either a domain admin account, an admin account, or a
   * delegated admin account. If the account does not fall under any of these admin categories, a
   * ServiceException with a permission denied message is thrown.
   *
   * @param account The Account to be validated.
   * @throws ServiceException If the account is not an admin account, indicating a permission denied
   *     scenario.
   */
  private void validateAdminAccount(Account account) throws ServiceException {
    boolean isDomainAdminAccount =
        account.getBooleanAttr(ZAttrProvisioning.A_zimbraIsDomainAdminAccount, false);
    boolean isAdminAccount =
        account.getBooleanAttr(ZAttrProvisioning.A_zimbraIsAdminAccount, false);
    boolean isDelegatedAdminAccount =
        account.getBooleanAttr(ZAttrProvisioning.A_zimbraIsDelegatedAdminAccount, false);
    if (!(isDomainAdminAccount || isAdminAccount || isDelegatedAdminAccount)) {
      throw ServiceException.PERM_DENIED("not an admin account");
    }
  }

  /**
   * Handles the validation of an inactive account.
   *
   * <p>Checks the status of the provided account using the specified Provisioning instance. If the
   * account status is set to "maintenance," an AccountServiceException with maintenance mode is
   * thrown. Otherwise, an AccountServiceException indicating that the account is inactive is
   * thrown.
   *
   * @param account The Account to be validated for inactivity.
   * @param provisioning The Provisioning instance to retrieve the account status.
   * @throws AccountServiceException If the account is in maintenance mode or inactive.
   * @see AccountServiceException
   * @see Provisioning
   */
  private void handleInactiveAccount(Account account, Provisioning provisioning)
      throws AccountServiceException {
    String accountStatus = account.getAccountStatus(provisioning);

    if (Provisioning.ACCOUNT_STATUS_MAINTENANCE.equalsIgnoreCase(accountStatus)) {
      throw AccountServiceException.MAINTENANCE_MODE();
    } else {
      throw AccountServiceException.ACCOUNT_INACTIVE(account.getName());
    }
  }

  /**
   * Validates the provided AuthToken.
   *
   * <p>Checks if the AuthToken is null, expired, or not registered. If any of these conditions are
   * met, an AuthTokenException is thrown with the appropriate error message.
   *
   * @param authToken The AuthToken to be validated.
   * @throws AuthTokenException If the AuthToken is null, expired, or not registered.
   * @see AuthToken
   * @see AuthTokenException
   */
  private void validateAuthToken(AuthToken authToken) throws AuthTokenException {
    if (authToken == null) {
      throw new AuthTokenException(
          "unable to get auth token from " + PreAuthParams.PARAM_AUTHTOKEN.getParamName());
    } else if (authToken.isExpired()) {
      throw new AuthTokenException("auth token expired");
    } else if (!authToken.isRegistered()) {
      throw new AuthTokenException("authtoken is invalid/or not registered");
    }
  }

  /**
   * Regenerates the authentication token to be used for pre-authentication. If the provided
   * authToken is an instance of ZimbraAuthToken, a new token is created by cloning the original one
   * and resetting its token ID. The original token is then de-registered, and the new token is
   * returned.
   *
   * @param authToken The original authentication token to be regenerated.
   * @return The regenerated authentication token with a new token ID.
   * @throws ServletException If there is an issue with cloning the ZimbraAuthToken or deregistering
   *     the original token.
   */
  private AuthToken regenerateAuthToken(AuthToken authToken)
      throws ServletException, AuthTokenException {
    if (authToken instanceof ZimbraAuthToken) {
      ZimbraAuthToken oneTimeToken = (ZimbraAuthToken) authToken;
      ZimbraAuthToken newZimbraAuthToken;
      try {
        newZimbraAuthToken = oneTimeToken.clone();
      } catch (CloneNotSupportedException e) {
        throw new ServletException(e);
      }
      newZimbraAuthToken.resetTokenId();

      oneTimeToken.deRegister();
      authToken = newZimbraAuthToken;
      ZimbraLog.account.debug(
          "De-registered the one time preauth token and issuing new one to the user.");
    }
    return authToken;
  }

  /**
   * Creates an auto-provisioned account based on the provided AutoProvisioningParams.
   *
   * <p>If the autoProvisioningParams specify auto-provisioning by name and the account is not an
   * admin account, the method attempts to auto-provision the account using the information from the
   * parameters. It pre-authenticates the account and then proceeds to auto-provision the account
   * based on the domain and account identifier provided in the AutoProvisioningParams.
   *
   * @param autoProvisioningParams The AutoProvisioningParams containing information for
   *     auto-provisioning.
   * @return The auto-provisioned Account if the auto-provisioning is successful, or null otherwise.
   * @throws ServiceException If an error occurs during the auto-provisioning process.
   * @see AutoProvisioningParams
   * @see AccountBy
   * @see EmailAddress
   * @see Domain
   * @see AutoProvAuthMech
   */
  private Account createAutoProvisionedAccount(AutoProvisioningParams autoProvisioningParams)
      throws ServiceException {
    Account acct = null;

    if (autoProvisioningParams.getAccountBy() == AccountBy.name
        && !autoProvisioningParams.isAdmin()) {
      // Try auto provision the account
      EmailAddress email = new EmailAddress(autoProvisioningParams.getAccountIdentifier(), false);
      String domainName = email.getDomain();
      Domain domain =
          domainName == null
              ? null
              : autoProvisioningParams.getProvisioning().get(Key.DomainBy.name, domainName);

      try {
        autoProvisioningParams
            .getProvisioning()
            .preAuthAccount(
                domain,
                autoProvisioningParams.getAccountIdentifier(),
                autoProvisioningParams.getAccountBy().name(),
                autoProvisioningParams.getTimestamp(),
                autoProvisioningParams.getExpires(),
                autoProvisioningParams.getPreAuth(),
                autoProvisioningParams.getAuthContext());
        acct =
            autoProvisioningParams
                .getProvisioning()
                .autoProvAccountLazy(
                    domain,
                    autoProvisioningParams.getAccountIdentifier(),
                    null,
                    AutoProvAuthMech.PREAUTH);

        if (acct != null) {
          ZimbraLog.account.debug(
              "Account auto-provisioned successfully for "
                  + autoProvisioningParams.getAccountIdentifier());
        }
      } catch (AuthFailedServiceException e) {
        ZimbraLog.account.debug(
            "Auth failed, unable to auto-provision account "
                + autoProvisioningParams.getAccountIdentifier(),
            e);
      } catch (ServiceException e) {
        ZimbraLog.account.info(
            "Unable to auto-provision account " + autoProvisioningParams.getAccountIdentifier(), e);
      }
    }

    return acct;
  }

  /**
   * Checks whether the current request needs to be referred to another server based on the account,
   * referral mode, and redirect flag.
   *
   * @param acct The Account associated with the user.
   * @param referMode The referral mode to determine if the redirection is needed.
   * @param isRedirect A boolean flag indicating whether the request is already a redirect.
   * @return true if the request needs to be redirected, false otherwise.
   * @throws ServiceException If there is an issue with the service processing some involved calls.
   */
  boolean needReferral(Account acct, String referMode, boolean isRedirect) throws ServiceException {
    // If this request is already a redirect, don't redirect again
    if (isRedirect) {
      return true;
    }

    return (!Provisioning.MAIL_REFER_MODE_ALWAYS.equals(referMode)
        && (!Provisioning.MAIL_REFER_MODE_WRONGHOST.equals(referMode)
            || Provisioning.onLocalServer(acct)));
  }

  /**
   * Adds query parameters from the HttpServletRequest to the given StringBuilder. Optionally,
   * non-pre-authentication parameters can be filtered out.
   *
   * @param req The HttpServletRequest object representing the user's request.
   * @param sb The StringBuilder to which query parameters will be appended.
   * @param first A boolean flag to indicate whether it's the first parameter being added.
   * @param nonPreAuthParamsOnly A boolean flag to indicate whether only non-pre-authentication
   *     parameters should be added to the StringBuilder.
   */
  private void addQueryParams(
      HttpServletRequest req, StringBuilder sb, boolean first, boolean nonPreAuthParamsOnly) {
    Enumeration<String> names = req.getParameterNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement();

      if (nonPreAuthParamsOnly && PreAuthParams.getPreAuthParams().contains(name)) {
        // Skip adding this parameter as it is a pre-authentication parameter and we are filtering
        // out non-pre-authentication parameters.
        continue;
      }

      String[] values = req.getParameterValues(name);
      if (values != null) {
        for (String value : values) {
          if (first) {
            first = false;
          } else {
            sb.append('&');
          }
          sb.append(name).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
      }
    }
  }

  /**
   * Redirects the current request to the correct server based on the provided account and token
   * information.
   *
   * @param req The HttpServletRequest object representing the user's request.
   * @param resp The HttpServletResponse object to use for the redirection.
   * @param acct The Account associated with the user.
   * @param token The authentication token, if available, or null if not present.
   * @throws ServiceException If there is an issue with the service processing some involved calls.
   * @throws IOException If there is an I/O error during the redirection.
   */
  private void redirectToCorrectServer(
      HttpServletRequest req, HttpServletResponse resp, Account acct, String token)
      throws ServiceException, IOException {
    StringBuilder sb = new StringBuilder();
    Provisioning provisioning = Provisioning.getInstance();

    // Append the base URL with the request URI and set the "isredirect" parameter
    sb.append(URLUtil.getServiceURL(provisioning.getServer(acct), req.getRequestURI(), true));
    sb.append('?').append(PreAuthParams.PARAM_IS_REDIRECT.getParamName()).append('=').append('1');

    if (token != null) {
      sb.append('&').append(PreAuthParams.PARAM_AUTHTOKEN.getParamName()).append('=').append(token);
      // Send only non-preauth (customer's) params over, as preauth params would be useless with a
      // token
      addQueryParams(req, sb, false, true);
    } else {
      // Send all incoming params over
      addQueryParams(req, sb, false, false);
    }

    // Perform the redirection
    resp.sendRedirect(sb.toString());
  }

  /**
   * Sets the authentication cookie in the response, sanitizes the redirect URL if provided, and
   * performs the appropriate redirection based on the user type (admin or non-admin).
   *
   * @param req The HttpServletRequest object representing the user's request.
   * @param resp The HttpServletResponse object to use for setting the cookie and redirection.
   * @param authToken The AuthToken associated with the user.
   * @throws IOException If there is an I/O error during the redirection.
   * @throws ServiceException If there is an issue with the service processing some involved calls.
   */
  void setCookieAndRedirect(HttpServletRequest req, HttpServletResponse resp, AuthToken authToken)
      throws IOException, ServiceException {
    boolean isAdmin = AuthToken.isAnyAdmin(authToken);
    boolean secureCookie = req.getScheme().equals("https");
    authToken.encode(resp, isAdmin, secureCookie);

    String redirectURL =
        Utils.getOptionalParam(req, PreAuthParams.PARAM_REDIRECT_URL.getParamName(), null);

    try {
      redirectURL = Utils.convertRedirectURLRelativeToContext(redirectURL);
    } catch (MalformedURLException e) {
      ZimbraLog.account.debug(String.format("URL %s is a malformed URL", redirectURL), e);
    }

    if (redirectURL != null) {
      resp.sendRedirect(redirectURL);
    } else {
      StringBuilder sb = new StringBuilder();
      addQueryParams(req, sb, true, true);
      redirectToApp(Utils.getBaseUrl(req), resp, authToken, isAdmin, sb);
    }
  }

  /**
   * Redirects the user to the appropriate application based on the user type (admin or non-admin
   * aka user) and any additional query parameters.
   *
   * @param baseUrl The base URL to redirect to.
   * @param resp The HttpServletResponse object to use for redirecting.
   * @param authToken The AuthToken associated with the user.
   * @param isAdmin Boolean flag indicating whether the user is an admin or not.
   * @param sb StringBuilder containing any additional query parameters to append to the URL.
   * @throws ServiceException If there is an issue with the service processing some involved calls.
   * @throws IOException If there is an I/O error during the redirection.
   */
  void redirectToApp(
      String baseUrl,
      HttpServletResponse resp,
      AuthToken authToken,
      boolean isAdmin,
      StringBuilder sb)
      throws ServiceException, IOException {
    Provisioning provisioning = Provisioning.getInstance();
    Server server = provisioning.getServer(authToken.getAccount());

    String redirectUrl;

    if (isAdmin) {
      String carbonioAdminProxyPort =
          server.getAttr(ZAttrProvisioning.A_carbonioAdminProxyPort, "6071");
      redirectUrl =
          baseUrl
              + ":"
              + carbonioAdminProxyPort
              + server.getAttr(ZAttrProvisioning.A_zimbraAdminURL, DEFAULT_ADMIN_URL);
    } else {
      redirectUrl = server.getAttr(ZAttrProvisioning.A_zimbraMailURL, DEFAULT_MAIL_URL);
    }

    // Append the query parameters if available
    if (sb.length() > 0) {
      redirectUrl += "?" + sb;
    }

    resp.sendRedirect(redirectUrl);
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.account;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.account.ZAttrProvisioning.AutoProvAuthMech;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.HeaderConstants;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AccountServiceException.AuthFailedServiceException;
import com.zimbra.cs.account.AttributeFlag;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthToken.TokenType;
import com.zimbra.cs.account.AuthToken.Usage;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.AuthMode;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.auth.AuthContext;
import com.zimbra.cs.account.krb5.Krb5Principal;
import com.zimbra.cs.account.names.NameUtil.EmailAddress;
import com.zimbra.cs.listeners.AuthListener;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.util.JWTUtil;
import com.zimbra.cs.servlet.CsrfFilter;
import com.zimbra.cs.servlet.util.CsrfUtil;
import com.zimbra.cs.session.Session;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

/**
 * @author schemers
 */
public class Auth extends AccountDocumentHandler {

  // for auth by auth token
  public static void addAccountToLogContextByAuthToken(Provisioning prov, AuthToken at) {
    String id = at.getAccountId();
    if (id != null) {
      AccountUtil.addAccountToLogContext(prov, id, ZimbraLog.C_NAME, ZimbraLog.C_ID, null);
    }
    String aid = at.getAdminAccountId();
    if (aid != null && !aid.equals(id)) {
      AccountUtil.addAccountToLogContext(prov, aid, ZimbraLog.C_ANAME, ZimbraLog.C_AID, null);
    }
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    // Look up the specified account.  It is optional in the <authToken> case.
    String acctValuePassedIn = null, acctValue = null, acctByStr = null;
    AccountBy acctBy = null;
    Account acct = null;
    Element acctEl = request.getOptionalElement(AccountConstants.E_ACCOUNT);
    boolean csrfSupport = request.getAttributeBool(AccountConstants.A_CSRF_SUPPORT, false);
    String reqTokenType = request.getAttribute(AccountConstants.A_TOKEN_TYPE, "");
    TokenType tokenType = TokenType.fromCode(reqTokenType);
    if (TokenType.JWT.equals(tokenType)) {
      // in case of jwt, csrfSupport has no significance
      csrfSupport = false;
    }
    ZimbraLog.account.debug("auth: reqTokenType: %s, tokenType: %s", reqTokenType, tokenType);
    if (acctEl != null) {
      acctValuePassedIn = acctEl.getText();
      acctValue = acctValuePassedIn;
      acctByStr = acctEl.getAttribute(AccountConstants.A_BY, AccountBy.name.name());
      acctBy = AccountBy.fromString(acctByStr);
      if (acctBy == AccountBy.name) {
        Element virtualHostEl = request.getOptionalElement(AccountConstants.E_VIRTUAL_HOST);
        String virtualHost = virtualHostEl == null ? null : virtualHostEl.getText().toLowerCase();
        if (virtualHost != null && acctValue.indexOf('@') == -1) {
          Domain d = prov.get(Key.DomainBy.virtualHostname, virtualHost);
          if (d != null) {
            acctValue = acctValue + "@" + d.getName();
          }
        }
      }
      acct = prov.get(acctBy, acctValue);
    }

    String password = request.getAttribute(AccountConstants.E_PASSWORD, null);
    String recoveryCode = request.getAttribute(AccountConstants.E_RECOVERY_CODE, null);

    Element authTokenEl = request.getOptionalElement(AccountConstants.E_AUTH_TOKEN);
    Element jwtTokenElement = request.getOptionalElement(AccountConstants.E_JWT_TOKEN);
    boolean validationSuccess =
        tokenTypeAndElementValidation(tokenType, authTokenEl, jwtTokenElement);
    if (!validationSuccess) {
      AuthFailedServiceException e =
          AuthFailedServiceException.AUTH_FAILED(
              "auth: incorrect tokenType and Element combination");
      AuthListener.invokeOnException(e);
      throw e;
    }
    boolean acctAutoProvisioned = false;
    Claims claims = null;
    // if jwtToken is present in request then use it
    if (jwtTokenElement != null && authTokenEl == null) {
      String jwt = jwtTokenElement.getText();
      String salt = JWTUtil.getSalt(null, context);
      claims = JWTUtil.validateJWT(jwt, salt);
      acct = prov.getAccountById(claims.getSubject());
      if (acct == null) {
        throw AccountServiceException.NO_SUCH_ACCOUNT(claims.getSubject());
      }
      acctAutoProvisioned = true;
    }
    // if authToken is present in request then use it
    if (authTokenEl != null) {
      boolean verifyAccount =
          authTokenEl.getAttributeBool(AccountConstants.A_VERIFY_ACCOUNT, false);
      if (verifyAccount && acctEl == null) {
        throw ServiceException.INVALID_REQUEST(
            "missing required element: " + AccountConstants.E_ACCOUNT, null);
      }
      try {
        AuthToken at = AuthProvider.getAuthToken(authTokenEl, acct);

        addAccountToLogContextByAuthToken(prov, at);

        // this could've been done in the very beginning of the method,
        // we do it here instead - after the account is added to log context
        // so the account will show in log context
        if (!checkPasswordSecurity(context)) {
          throw ServiceException.INVALID_REQUEST("clear text password is not allowed", null);
        }
        AuthToken.Usage usage = at.getUsage();
        Account authTokenAcct = AuthProvider.validateAuthToken(prov, at, false, usage);
        if (verifyAccount) {
          // Verify the named account matches the account in the auth token.  Client can easily
          // decode
          // the auth token and do this check, but doing it on the server is nice because then the
          // client
          // can treat the auth token as an opaque string.
          if (acct == null || !acct.getId().equalsIgnoreCase(authTokenAcct.getId())) {
            throw new AuthTokenException("auth token doesn't match the named account");
          }
        }
        if (usage == Usage.AUTH) {
          ServletRequest httpReq = (ServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
          httpReq.setAttribute(CsrfFilter.AUTH_TOKEN, at);
          if (csrfSupport && !at.isCsrfTokenEnabled()) {
            // handle case where auth token was originally generated with csrf support
            // and now client sends the same auth token but saying csrfSupport is turned off
            // in that case do not disable CSRF check for this authToken.
            at.setCsrfTokenEnabled(csrfSupport);
          }
          return doResponse(request, at, zsc, context, authTokenAcct, csrfSupport);
        } else {
          acct = authTokenAcct;
        }
      } catch (AuthTokenException e) {
        throw ServiceException.AUTH_REQUIRED();
      }
    }
    if (!checkPasswordSecurity(context)) {
      throw ServiceException.INVALID_REQUEST("clear text password is not allowed", null);
    }

    Element preAuthEl = request.getOptionalElement(AccountConstants.E_PREAUTH);
    long expires = 0;

    Map<String, Object> authCtxt = new HashMap<>();
    authCtxt.put(AuthContext.AC_ORIGINATING_CLIENT_IP, context.get(SoapEngine.ORIG_REQUEST_IP));
    authCtxt.put(AuthContext.AC_REMOTE_IP, context.get(SoapEngine.SOAP_REQUEST_IP));
    authCtxt.put(AuthContext.AC_ACCOUNT_NAME_PASSEDIN, acctValuePassedIn);
    authCtxt.put(AuthContext.AC_USER_AGENT, zsc.getUserAgent());

    AuthMode mode = AuthMode.PASSWORD;
    String code = password;
    if (StringUtils.isEmpty(password) && StringUtils.isNotEmpty(recoveryCode)) {
      mode = AuthMode.RECOVERY_CODE;
      code = recoveryCode;
    }
    authCtxt.put(Provisioning.AUTH_MODE_KEY, mode);

    if (acct == null) {
      // try LAZY auto provision if it is enabled
      if (acctBy == AccountBy.name || acctBy == AccountBy.krb5Principal) {
        try {
          if (acctBy == AccountBy.name) {
            EmailAddress email = new EmailAddress(acctValue, false);
            String domainName = email.getDomain();
            Domain domain = domainName == null ? null : prov.get(Key.DomainBy.name, domainName);
            if (password != null) {
              acct = prov.autoProvAccountLazy(domain, acctValuePassedIn, password, null);
            } else if (preAuthEl != null) {
              long timestamp = preAuthEl.getAttributeLong(AccountConstants.A_TIMESTAMP);
              expires = preAuthEl.getAttributeLong(AccountConstants.A_EXPIRES, 0);
              String preAuth = preAuthEl.getTextTrim();
              prov.preAuthAccount(
                  domain, acctValue, acctByStr, timestamp, expires, preAuth, authCtxt);

              acct =
                  prov.autoProvAccountLazy(
                      domain, acctValuePassedIn, null, AutoProvAuthMech.PREAUTH);
            }
          } else {
            if (password != null) {
              Domain domain = Krb5Principal.getDomainByKrb5Principal(acctValuePassedIn);
              if (domain != null) {
                acct = prov.autoProvAccountLazy(domain, acctValuePassedIn, password, null);
              }
            }
          }

          if (acct != null) {
            acctAutoProvisioned = true;
          }
        } catch (AuthFailedServiceException e) {
          AuthListener.invokeOnException(e);
          ZimbraLog.account.debug("auth failed, unable to auto provisioing acct " + acctValue, e);
        } catch (ServiceException e) {
          ZimbraLog.account.info("unable to auto provisioing acct " + acctValue, e);
        }
      }
    }

    if (acct == null) {
      AuthFailedServiceException e =
          AuthFailedServiceException.AUTH_FAILED(acctValue, acctValuePassedIn, "account not found");
      AuthListener.invokeOnException(e);
      throw e;
    }

    AccountUtil.addAccountToLogContext(prov, acct.getId(), ZimbraLog.C_NAME, ZimbraLog.C_ID, null);
    // if account was auto provisioned, we had already authenticated the principal
    if (!acctAutoProvisioned) {
      if (password != null || recoveryCode != null) {
        prov.authAccount(acct, code, AuthContext.Protocol.soap, authCtxt);
      } else if (preAuthEl != null) {
        long timestamp = preAuthEl.getAttributeLong(AccountConstants.A_TIMESTAMP);
        expires = preAuthEl.getAttributeLong(AccountConstants.A_EXPIRES, 0);
        String preAuth = preAuthEl.getTextTrim();
        prov.preAuthAccount(acct, acctValue, acctByStr, timestamp, expires, preAuth, authCtxt);
      } else {
        throw ServiceException.INVALID_REQUEST("must specify " + AccountConstants.E_PASSWORD, null);
      }
    }

    final AuthToken authToken =
        expires == 0
            ? AuthProvider.getAuthToken(acct, tokenType)
            : AuthProvider.getAuthToken(acct, expires, tokenType);

    ServletRequest httpReq = (ServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
    // For CSRF filter so that token generation can happen
    if (csrfSupport && !authToken.isCsrfTokenEnabled()) {
      // handle case where auth token was originally generated with csrf support
      // and now client sends the same auth token but saying csrfSupport is turned off
      // in that case do not disable CSRF check for this authToken.
      authToken.setCsrfTokenEnabled(csrfSupport);
    }
    httpReq.setAttribute(CsrfFilter.AUTH_TOKEN, authToken);
    AuthListener.invokeOnSuccess(acct);
    return doResponse(request, authToken, zsc, context, acct, csrfSupport);
  }

  private boolean tokenTypeAndElementValidation(
      TokenType tokenType, Element authElem, Element jwtElem) {
    if (jwtElem != null && authElem != null) {
      ZimbraLog.account.debug("both jwt and auth element can not be present in auth request");
      return Boolean.FALSE;
    }
    if (jwtElem == null && authElem != null && TokenType.JWT.equals(tokenType)) {
      ZimbraLog.account.debug("jwt token type not supported with auth element");
      return Boolean.FALSE;
    }
    if (jwtElem != null && authElem == null && !TokenType.JWT.equals(tokenType)) {
      ZimbraLog.account.debug("auth token type not supported with jwt element");
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

  private Element doResponse(
      Element request,
      AuthToken at,
      ZimbraSoapContext zsc,
      Map<String, Object> context,
      Account acct,
      boolean csrfSupport)
      throws ServiceException {
    Element response = zsc.createElement(AccountConstants.AUTH_RESPONSE);
    at.encodeAuthResp(response, false);

    /*
     * bug 67078
     * also return auth token cookie in http header
     */
    HttpServletRequest httpReq = (HttpServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
    HttpServletResponse httpResp = (HttpServletResponse) context.get(SoapServlet.SERVLET_RESPONSE);
    boolean rememberMe =
        request.getAttributeBool(AccountConstants.A_PERSIST_AUTH_TOKEN_COOKIE, false);
    at.encode(httpReq, httpResp, false, ZimbraCookie.secureCookie(httpReq), rememberMe);

    response.addAttribute(
        AccountConstants.E_LIFETIME,
        at.getExpires() - System.currentTimeMillis(),
        Element.Disposition.CONTENT);
    boolean isCorrectHost = Provisioning.getInstance().onLocalServer(acct);
    if (isCorrectHost) {
      Session session = updateAuthenticatedAccount(zsc, at, context, true);
      if (session != null) {
        ZimbraSoapContext.encodeSession(response, session.getSessionId(), session.getSessionType());
      }
    }

    Server localhost = Provisioning.getInstance().getLocalServer();
    String referMode = localhost.getAttr(ZAttrProvisioning.A_zimbraMailReferMode, "wronghost");
    if (Provisioning.MAIL_REFER_MODE_ALWAYS.equals(referMode)
        || (Provisioning.MAIL_REFER_MODE_WRONGHOST.equals(referMode) && !isCorrectHost)) {
      response.addAttribute(
          AccountConstants.E_REFERRAL,
          acct.getAttr(ZAttrProvisioning.A_zimbraMailHost),
          Element.Disposition.CONTENT);
    }

    Element prefsRequest = request.getOptionalElement(AccountConstants.E_PREFS);
    if (prefsRequest != null) {
      Element prefsResponse = response.addUniqueElement(AccountConstants.E_PREFS);
      GetPrefs.handle(prefsRequest, prefsResponse, acct);
    }

    Element attrsRequest = request.getOptionalElement(AccountConstants.E_ATTRS);
    if (attrsRequest != null) {
      Element attrsResponse = response.addUniqueElement(AccountConstants.E_ATTRS);
      Set<String> attrList =
          AttributeManager.getInstance().getAttrsWithFlag(AttributeFlag.accountInfo);
      for (Iterator it = attrsRequest.elementIterator(AccountConstants.E_ATTR); it.hasNext(); ) {
        Element e = (Element) it.next();
        String name = e.getAttribute(AccountConstants.A_NAME);
        if (name != null && attrList.contains(name)) {
          Object v = acct.getUnicodeMultiAttr(name);
          if (v != null) {
            ToXML.encodeAttr(attrsResponse, name, v);
          }
        }
      }
    }

    boolean csrfCheckEnabled = false;
    if (httpReq.getAttribute(ZAttrProvisioning.A_zimbraCsrfTokenCheckEnabled) != null) {
      csrfCheckEnabled =
          (Boolean) httpReq.getAttribute(ZAttrProvisioning.A_zimbraCsrfTokenCheckEnabled);
    }

    if (csrfSupport && csrfCheckEnabled) {
      String accountId = at.getAccountId();
      long authTokenExpiration = at.getExpires();
      int tokenSalt = (Integer) httpReq.getAttribute(CsrfFilter.CSRF_SALT);
      String token = CsrfUtil.generateCsrfToken(accountId, authTokenExpiration, tokenSalt, at);
      Element csrfResponse = response.addUniqueElement(HeaderConstants.E_CSRFTOKEN);
      csrfResponse.addText(token);
      httpResp.setHeader(Constants.CSRF_TOKEN, token);
    }

    Element productQueryEl = request.getOptionalElement(AccountConstants.E_PRODUCT_QUERY);
    String productQueryElStr = productQueryEl != null ? productQueryEl.getText() : "0";
    boolean reqContainsProductQuery =
        productQueryElStr.equalsIgnoreCase("true") || productQueryElStr.equalsIgnoreCase("1");
    if (reqContainsProductQuery) {
      response
          .addUniqueElement(AccountConstants.E_PRODUCT_QUERY)
          .addUniqueElement("productName")
          .setText("Carbonio");
    }
    return response;
  }

  @Override
  public boolean needsAuth(Map<String, Object> context) {
    return false;
  }
}

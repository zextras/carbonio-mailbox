// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on May 26, 2004
 */
package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.HeaderConstants;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.ZimbraCookie;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException.AuthFailedServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Provisioning.AuthMode;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.auth.AuthContext;
import com.zimbra.cs.account.auth.AuthMechanism.AuthMech;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.servlet.CsrfFilter;
import com.zimbra.cs.servlet.util.CsrfUtil;
import com.zimbra.cs.session.Session;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

public class Auth extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        AuthToken at = null;
        Account acct = null;

        Provisioning prov = Provisioning.getInstance();
        boolean csrfSupport = request.getAttributeBool(AccountConstants.A_CSRF_SUPPORT, false);
        String name = request.getAttribute(AdminConstants.E_NAME, null);
        Element acctEl = request.getOptionalElement(AccountConstants.E_ACCOUNT);

        //only perform auth-token authentication if other credentials are not provided
        if (name == null && acctEl == null) {
            //get an auth token from cookie
            at = zsc.getAuthToken();
            if(at == null) {
                //if auth token is not in the cookie check for auth token in SOAP
                Element authTokenEl = request.getOptionalElement(AdminConstants.E_AUTH_TOKEN);
                if(authTokenEl != null) {
                    try {
                        at = AuthProvider.getAuthToken(request, new HashMap<String, Object>());
                    } catch (AuthTokenException e) {
                        throw ServiceException.AUTH_REQUIRED();
                    }
                }
            }

            if(at == null) {
                //neither login credentials nor valid auth token could be retrieved
                throw ServiceException.AUTH_REQUIRED();
            }
            com.zimbra.cs.service.account.Auth.addAccountToLogContextByAuthToken(prov, at);

            if (at.isExpired())
                throw ServiceException.AUTH_EXPIRED();

            if(!at.isRegistered())
                throw ServiceException.AUTH_EXPIRED("authtoken is invalid");

            // make sure that the authenticated account is active and has not been deleted/disabled since the last request
            acct = prov.get(AccountBy.id, at.getAccountId(), at);
            if (acct == null || !acct.getAccountStatus(prov).equals(Provisioning.ACCOUNT_STATUS_ACTIVE))
                throw ServiceException.AUTH_EXPIRED();

            // make sure the authenticated account is an admin account
            checkAdmin(acct);
        } else {
            /*
             * only one of
             *     <name>...</name>
             * or
             *     <account by="name|id|foreignPrincipal">...</account>
             * can/must be specified
             */
            if (name != null && acctEl != null)
                throw ServiceException.INVALID_REQUEST("only one of <name> or <account> can be specified", null);
            if (name == null && acctEl == null)
                throw ServiceException.INVALID_REQUEST("missing <name> or <account>", null);

            String password = request.getAttribute(AdminConstants.E_PASSWORD);
            Element virtualHostEl = request.getOptionalElement(AccountConstants.E_VIRTUAL_HOST);
            String virtualHost = virtualHostEl == null ? null : virtualHostEl.getText().toLowerCase();

            String valuePassedIn;
            AccountBy by;
            String value;
            if (name != null) {
                valuePassedIn = name;
                by = AccountBy.name;
            } else {
                valuePassedIn = acctEl.getText();
                String byStr = acctEl.getAttribute(AccountConstants.A_BY, AccountBy.name.name());
                by = AccountBy.fromString(byStr);
            }
            value = valuePassedIn;

            try {

                if (by == AccountBy.name && !value.contains("@")) {
                    // first try to get by adminName, which resolves the account under cn=admins,cn=zimbra
                    // and does not need a domain
                    acct = prov.get(AccountBy.adminName, value, zsc.getAuthToken());

                    // not found, try applying virtual host name
                    if (acct == null) {
                        if (virtualHost != null) {
                            Domain d = prov.get(Key.DomainBy.virtualHostname, virtualHost);
                            if (d != null)
                                value = value + "@" + d.getName();
                        }
                    }
                }

                if (acct == null)
                    acct = prov.get(by, value);

                if (acct == null)
                    throw AuthFailedServiceException.AUTH_FAILED(value, valuePassedIn, "account not found");

                checkAdmin(acct);

                AccountUtil.addAccountToLogContext(prov, acct.getId(), ZimbraLog.C_NAME, ZimbraLog.C_ID, null);

                ZimbraLog.security.info(ZimbraLog.encodeAttrs(
                        new String[] {"cmd", "AdminAuth","account", value}));

                Map<String, Object> authCtxt = new HashMap<String, Object>();
                authCtxt.put(AuthContext.AC_ORIGINATING_CLIENT_IP, context.get(SoapEngine.ORIG_REQUEST_IP));
                authCtxt.put(AuthContext.AC_REMOTE_IP, context.get(SoapEngine.SOAP_REQUEST_IP));
                authCtxt.put(AuthContext.AC_ACCOUNT_NAME_PASSEDIN, valuePassedIn);
                authCtxt.put(AuthContext.AC_USER_AGENT, zsc.getUserAgent());
                authCtxt.put(AuthContext.AC_AS_ADMIN, Boolean.TRUE);          

				// Only perform 2fa authentication on admin account not on the resource accounts
				if (!acct.isIsSystemResource() && !acct.isIsSystemAccount()) {
          String recoveryCode = request.getAttribute(AccountConstants.E_RECOVERY_CODE, null);
          AuthMode mode = AuthMode.PASSWORD;
          String code = password;
          if (StringUtils.isEmpty(password) && StringUtils.isNotEmpty(recoveryCode)) {
            mode = AuthMode.RECOVERY_CODE;
            code = recoveryCode;
          }
          authCtxt.put(Provisioning.AUTH_MODE_KEY, mode);
          if (password != null || recoveryCode != null) {
            prov.authAccount(acct, code, AuthContext.Protocol.soap, authCtxt);
          }
				} else {
					// perform authAccount for the zmprov account directly as it can't have 2fa
					prov.authAccount(acct, password, AuthContext.Protocol.soap, authCtxt);
				}
                AuthMech authedByMech = (AuthMech) authCtxt.get(AuthContext.AC_AUTHED_BY_MECH);
                at = AuthProvider.getAuthToken(acct, true, authedByMech);
            } catch (ServiceException se) {
                ZimbraLog.security.warn(ZimbraLog.encodeAttrs(
                        new String[] {"cmd", "AdminAuth","account", value, "error", se.getMessage()}));
                throw se;
            }
        }
        if(at != null) {
            at.setCsrfTokenEnabled(csrfSupport);
        }
        ServletRequest httpReq = (ServletRequest) context.get(SoapServlet.SERVLET_REQUEST);
        httpReq.setAttribute(CsrfFilter.AUTH_TOKEN, at);
        return doResponse(request, at, zsc, context, acct, csrfSupport);
    }

    private void checkAdmin(Account acct) throws ServiceException {
        boolean isDomainAdmin = acct.getBooleanAttr(Provisioning.A_zimbraIsDomainAdminAccount, false);
        boolean isAdmin= acct.getBooleanAttr(Provisioning.A_zimbraIsAdminAccount, false);
        boolean isDelegatedAdmin= acct.getBooleanAttr(Provisioning.A_zimbraIsDelegatedAdminAccount, false);
        boolean ok = (isDomainAdmin || isAdmin || isDelegatedAdmin);
        if (!ok)
            throw ServiceException.PERM_DENIED("not an admin account");
    }
    
    private Element doResponse(Element request, AuthToken at, ZimbraSoapContext zsc, Map<String, Object> context, Account acct, boolean csrfSupport) throws ServiceException {
        Element response = zsc.createElement(AdminConstants.AUTH_RESPONSE);
        at.encodeAuthResp(response, true);

        /*
         * bug 67078
         * also return auth token cookie in http header
         */
        HttpServletRequest httpReq = (HttpServletRequest)context.get(SoapServlet.SERVLET_REQUEST);
        HttpServletResponse httpResp = (HttpServletResponse)context.get(SoapServlet.SERVLET_RESPONSE);
        boolean rememberMe = request.getAttributeBool(AdminConstants.A_PERSIST_AUTH_TOKEN_COOKIE, false);
        at.encode(httpResp, true, ZimbraCookie.secureCookie(httpReq), rememberMe);

        response.addAttribute(AdminConstants.E_LIFETIME, at.getExpires() - System.currentTimeMillis(), Element.Disposition.CONTENT);

        Session session = updateAuthenticatedAccount(zsc, at, context, true);
        if (session != null) {
            ZimbraSoapContext.encodeSession(response, session.getSessionId(), session.getSessionType());
        }

        setCSRFToken(httpReq, httpResp, at, csrfSupport, response);

        return response;
    }

    private void setCSRFToken(HttpServletRequest httpReq, HttpServletResponse httpResp, AuthToken authToken, boolean csrfSupport, Element response) throws ServiceException {
		boolean csrfCheckEnabled = false;
		if (httpReq.getAttribute(Provisioning.A_zimbraCsrfTokenCheckEnabled) != null) {
			csrfCheckEnabled = (Boolean) httpReq.getAttribute(Provisioning.A_zimbraCsrfTokenCheckEnabled);
		}
		if (csrfSupport && csrfCheckEnabled) {
			String accountId = authToken.getAccountId();
			long authTokenExpiration = authToken.getExpires();
			int tokenSalt = (Integer) httpReq.getAttribute(CsrfFilter.CSRF_SALT);
			String token = CsrfUtil.generateCsrfToken(accountId, authTokenExpiration, tokenSalt, authToken);
			Element csrfResponse = response.addUniqueElement(HeaderConstants.E_CSRFTOKEN);
			csrfResponse.addText(token);
			httpResp.setHeader(Constants.CSRF_TOKEN, token);
		}
	}

    @Override
    public boolean needsAuth(Map<String, Object> context) {
        // can't require auth on auth request
        return false;
    }

    @Override
    public boolean needsAdminAuth(Map<String, Object> context) {
        // can't require auth on auth request
        return false;
    }

    @Override
    public void docRights(List<AdminRight> relatedRights, List<String> notes) {
        notes.add(AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
    }
}

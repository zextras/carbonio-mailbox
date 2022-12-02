// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.account;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthToken.Usage;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.cs.service.util.ResetPasswordUtil;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.SoapServlet;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.account.message.ResetPasswordRequest;

public class ResetPassword extends AccountDocumentHandler {

	@Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        if (!checkPasswordSecurity(context)) {
            throw ServiceException.INVALID_REQUEST("clear text password is not allowed", null);
        }
        Provisioning prov = Provisioning.getInstance();
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        ResetPasswordRequest req = JaxbUtil.elementToJaxb(request);
        Element response = zsc.createElement(AccountConstants.E_RESET_PASSWORD_RESPONSE);

        AuthToken at = zsc.getAuthToken();
        if (at == null) {
            throw ServiceException.AUTH_REQUIRED("Auth Token missing.");
        }

        if(at.getUsage() == Usage.RESET_PASSWORD) {
            AuthProvider.validateAuthToken(prov, at, false, Usage.RESET_PASSWORD);
        } else {
            AuthProvider.validateAuthToken(prov, at, false);
        }
        
        Account acct = at.getAccount();
        boolean getPasswordRules = request.getAttributeBool(AccountConstants.E_GET_PASSWORD_RULES, false);
        boolean cancelResetPassword = request.getAttributeBool(AccountConstants.E_CANCEL_RESET_PASSWORD, false);
        boolean dryRun = request.getAttributeBool(AccountConstants.E_DRYRUN, false);
        HttpServletResponse httpResp = (HttpServletResponse) context.get(SoapServlet.SERVLET_RESPONSE);

        if (cancelResetPassword) {
            ZAuthToken.clearCookies(httpResp);
            return response;
        }

        if (getPasswordRules) {
            Element attrs = response.addUniqueElement(AccountConstants.E_ATTRS);
            ToXML.encodePasswordRules(attrs, acct);
            return response;
        }
        req.validateResetPasswordRequest();
        boolean locked = acct.getBooleanAttr(Provisioning.A_zimbraPasswordLocked, false);
        if (locked) {
            throw AccountServiceException.PASSWORD_LOCKED();
        }
        ResetPasswordUtil.validateFeatureResetPasswordStatus(acct);
        String newPassword = req.getPassword();

        // proxy if required
        if (!Provisioning.onLocalServer(acct)) {
            try {
                return proxyRequest(request, context, acct.getId());
            } catch (ServiceException e) {
                // if something went wrong proxying the request, just execute it locally
                if (ServiceException.PROXY_ERROR.equals(e.getCode())) {
                    ZimbraLog.account.warn("encountered proxy error", e);
                } else {
                    // but if it's a real error, it's a real error
                    throw e;
                }
            }
        }

        setPasswordAndPurgeAuthTokens(prov, acct, newPassword, dryRun);
        if (!dryRun) {
            acct.unsetResetPasswordRecoveryCode();
            ZAuthToken.clearCookies(httpResp);
        }

        return response;
    }

	protected void setPasswordAndPurgeAuthTokens(Provisioning prov, Account acct, String newPassword, 
	        boolean dryRun) throws ServiceException {
	    if (dryRun) {
	        prov.resetPassword(acct, newPassword, dryRun);
	    } else {
	        // set new password
	        prov.setPassword(acct, newPassword, true);
	        // purge old auth tokens to invalidate existing sessions
	        acct.purgeAuthTokens();
	    }
	}

    protected void checkPasswordStrength(Provisioning prov, Account acct, String newPassword) throws ServiceException {
        prov.checkPasswordStrength(acct, newPassword);
    }

    @Override
    public boolean needsAuth(Map<String, Object> context) {
        return false;
    }
}

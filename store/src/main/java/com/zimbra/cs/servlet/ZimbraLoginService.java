// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import java.security.Principal;
import java.util.function.Function;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;

import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.UserIdentity;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Session;
import org.eclipse.jetty.util.security.Credential;

import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException.AuthFailedServiceException;
import com.zimbra.cs.account.CacheAwareProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.auth.AuthContext;

/**
 * Jetty login service which handles HTTP BASIC authentication requests via
 * Zimbra Provisioning
 *
 */
public class ZimbraLoginService implements LoginService {

    protected IdentityService identityService = new DefaultIdentityService();
    protected String name;

    @Override
    public void setIdentityService(IdentityService idService) {
        identityService = idService;
    }

    @Override
    public IdentityService getIdentityService() {
        return identityService;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void logout(UserIdentity user) {
    }

    @Override
    public boolean validate(UserIdentity user) {
        return false;
    }

    @Override
    public UserIdentity login(String username, Object credentials, Request req, Function<Boolean, Session> sessionHandler) {
        Account account;
        try {
            Provisioning prov = Provisioning.getInstance();
            account = prov.get(AccountBy.name, username);
            if (account != null) {
                if (!(credentials instanceof String)) {
                    ZimbraLog.security.warn("passed credentials are not a String? [%s]", credentials == null ? "null" : credentials.getClass().getName());
                }
                tryLogin(account, (String) credentials, true);
                return makeUserIdentity(username);
            }
        } catch (AuthFailedServiceException e) {
            ZimbraLog.security.debug("Auth failed");
        } catch (ServiceException e) {
            ZimbraLog.security.warn("ServiceException in auth", e);
        }
        return null;
    }

    private void tryLogin(Account account, String credentials, boolean first) throws ServiceException {
        //try login, or pass along exception if failed
        Provisioning prov = Provisioning.getInstance();
        if (account != null) {
            try {
                prov.authAccount(account, credentials,
                    AuthContext.Protocol.http_basic);
            } catch (AuthFailedServiceException e) {
                if (!first || !(prov instanceof CacheAwareProvisioning) || !((CacheAwareProvisioning) prov).isCacheEnabled()) {
                    throw e;
                } else {
                    //we may have the old password cached; so reload and try one more time
                    ZimbraLog.security.debug("auth failed, refreshing Account object and trying again in case of recent password change");
                    prov.reload(account);
                    tryLogin(account, credentials, false);
                }
            }
        }
    }

    UserIdentity makeUserIdentity(String userName) {
        Credential credential = Credential.getCredential("");
        String roleName = "user";
        Principal userPrincipal = () -> userName;
        Principal rolePrincipal = () -> roleName;
        Subject subject = new Subject();
        subject.getPrincipals().add(userPrincipal);
        subject.getPrivateCredentials().add(credential);
        subject.getPrincipals().add(rolePrincipal);
        subject.setReadOnly();

        UserIdentity identity = identityService.newUserIdentity(subject,
                userPrincipal, new String[] { roleName });
        return identity;
    }
}

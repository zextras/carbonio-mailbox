// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.security.sasl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.security.sasl.SaslServer;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ZimbraAuthToken;
import com.zimbra.cs.account.auth.AuthContext;
import com.zimbra.cs.service.AuthProvider;

public class ZimbraAuthenticator extends Authenticator {
    public static final String MECHANISM = "X-ZIMBRA";
    private AuthToken authToken;

    public ZimbraAuthenticator(AuthenticatorUser user) {
        super(MECHANISM, user);
    }

    // X-ZIMBRA is supported in all protocols (IMAP, POP, etc.)
    @Override protected boolean isSupported()  { return true; }

    @Override public boolean initialize()  { return true; }
    @Override public void dispose()        { }

    @Override public boolean isEncryptionEnabled()  { return false; }

    @Override public InputStream unwrap(InputStream is)  { return null; }
    @Override public OutputStream wrap(OutputStream os)  { return null; }

    @Override public SaslServer getSaslServer()  { return null; }

    @Override public void handle(byte[] data) throws IOException {
        if (isComplete())
            throw new IllegalStateException("Authentication already completed");

        String message = new String(data, "utf-8");

        int nul1 = message.indexOf('\0'), nul2 = message.indexOf('\0', nul1 + 1);
        if (nul1 == -1 || nul2 == -1) {
            sendBadRequest();
            return;
        }
        String authorizeId = message.substring(0, nul1);
        String authenticateId = message.substring(nul1 + 1, nul2);
        String authtoken = message.substring(nul2 + 1);
        authenticate(authorizeId, authenticateId, authtoken);
    }

    @Override public Account authenticate(String username, String authenticateId, String authtoken,
                                          AuthContext.Protocol protocol, String origRemoteIp, String remoteIp, String userAgent)
    throws ServiceException {
        if (authenticateId == null || authenticateId.equals(""))
            return null;

        // validate the auth token
        Provisioning prov = Provisioning.getInstance();
        AuthToken at;
        try {
            at = ZimbraAuthToken.getAuthToken(authtoken);
        } catch (AuthTokenException e) {
            return null;
        }

        try {
            AuthProvider.validateAuthToken(prov, at, false);
        } catch (ServiceException e) {
            return null;
        }

        // make sure that the authentication account is valid
        Account authAccount = prov.get(at.isAdmin() ? Key.AccountBy.adminName : Key.AccountBy.name, authenticateId, at);
        if (authAccount == null)
            return null;

        // make sure the auth token belongs to authenticatedId
        if (!at.getAccountId().equalsIgnoreCase(authAccount.getId()))
            return null;

        // make sure the protocol is enabled for the user
        if (!isProtocolEnabled(authAccount, protocol)) {
            ZimbraLog.account.info("Authentication failed - %s not enabled for %s", protocol, authAccount.getName());
            return null;
        }

        Account targetAcct = authorize(authAccount, username, AuthToken.isAnyAdmin(at));
        if (targetAcct != null) {
            prov.accountAuthed(authAccount);
            this.authToken = at;
        }
        return targetAcct;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }
}

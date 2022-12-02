// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.pop3;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.security.sasl.Authenticator;
import com.zimbra.cs.security.sasl.AuthenticatorUser;

import java.io.IOException;

class Pop3AuthenticatorUser implements AuthenticatorUser {
    private final Pop3Handler mHandler;

    Pop3AuthenticatorUser(Pop3Handler handler) {
        mHandler = handler;
    }

    @Override
    public String getProtocol()  { return "pop"; }

    @Override
    public void sendBadRequest(String s) throws IOException {
        mHandler.sendERR(s);
    }

    @Override
    public void sendFailed() throws IOException {
        mHandler.sendERR("authentication failed");
    }

    @Override
    public void sendFailed(String msg) throws IOException {
        mHandler.sendERR("authentication failed: " + msg);
    }

    @Override
    public void sendSuccessful() throws IOException {
        mHandler.sendOK("authentication successful");
    }

    @Override
    public void sendContinuation(String s) throws IOException {
        mHandler.sendContinuation(s);
    }

    @Override
    public boolean authenticate(String authorizationId, String authenticationId, String password, Authenticator auth)
    throws IOException {
        try {
            mHandler.authenticate(authorizationId, authenticationId, password, auth);
        } catch (Pop3CmdException e) {
            auth.sendFailed(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Log getLog() {
        return ZimbraLog.pop;
    }

    @Override
    public boolean isSSLEnabled() {
        return mHandler.isSSLEnabled();
    }

    @Override
    public boolean allowCleartextLogin() {
        return mHandler.config.isCleartextLoginsEnabled();
    }

    @Override
    public boolean isGssapiAvailable() {
        return mHandler.config.isSaslGssapiEnabled();
    }
}

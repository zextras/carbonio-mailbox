// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.security.sasl.Authenticator;
import com.zimbra.cs.security.sasl.AuthenticatorUser;

import java.io.IOException;

class ImapAuthenticatorUser implements AuthenticatorUser {
    private final ImapHandler mHandler;
    private final String mTag;
    private boolean canContinue = true;

    ImapAuthenticatorUser(ImapHandler handler, String tag) {
        mHandler = handler;
        mTag = tag;
    }

    String getTag()  { return mTag; }

    boolean canContinue()  { return canContinue; }

    @Override public String getProtocol()  { return "imap"; }

    @Override public void sendBadRequest(String s) throws IOException {
        mHandler.sendBAD(mTag, s);
    }

    @Override public void sendFailed() throws IOException {
        mHandler.sendNO(mTag, "AUTHENTICATE failed");
    }

    @Override public void sendFailed(String msg) throws IOException {
        mHandler.sendNO(mTag, "AUTHENTICATE failed: " + msg);
    }

    @Override public void sendSuccessful() throws IOException {
        // 6.2.2: "A server MAY include a CAPABILITY response code in the tagged OK
        //         response of a successful AUTHENTICATE command in order to send
        //         capabilities automatically."
        mHandler.sendOK(mTag, '[' + mHandler.getCapabilityString() + "] AUTHENTICATE completed");
    }

    @Override public void sendContinuation(String s) throws IOException {
        mHandler.sendContinuation(s);
    }

    @Override public boolean authenticate(String authorizationId, String authenticationId, String password, Authenticator auth)
    throws IOException {
        canContinue = mHandler.authenticate(authorizationId, authenticationId, password, mTag, auth);
        return mHandler.isAuthenticated();
    }

    @Override public Log getLog() {
        return ZimbraLog.imap;
    }

    @Override public boolean isSSLEnabled() {
        return mHandler.isSSLEnabled();
    }

    @Override public boolean allowCleartextLogin() {
        return mHandler.getConfig().isCleartextLoginEnabled();
    }

    @Override public boolean isGssapiAvailable() {
        return mHandler.getConfig().isSaslGssapiEnabled();
    }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.yauth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

public final class RawAuthManager {
    private final TokenStore store;
    private final HashMap<String, RawAuth> cookies;

    private static final Logger LOG = LogManager.getLogger(RawAuthManager.class);

    public RawAuthManager(TokenStore store) {
        this.store = store;
        cookies = new HashMap<String, RawAuth>();
    }

    public RawAuth authenticate(String appId, String user, String pass)
        throws AuthenticationException, IOException {
        RawAuth auth = cookies.get(key(appId, user));
        if (auth == null || auth.isExpired()) {
            // Cookie missing or expired, so get a new one
            String token = store.getToken(appId, user);
            if (token == null) {
                // If token not found, generating a new one
                token = store.newToken(appId, user, pass);
            }
            try {
                auth = RawAuth.authenticate(appId, token);
            } catch (AuthenticationException e) {
                // If authentication failed, check for invalid token in which
                // case we will generate a new one and try again...
                switch (e.getErrorCode()) {
                case TOKEN_REQUIRED:
                case INVALID_TOKEN:
                    invalidate(appId, user);
                    token = store.newToken(appId, user, pass);
                    auth = RawAuth.authenticate(appId, token);
                    break;
                default:
                    throw e;
                }

            }
            cookies.put(key(appId, user), auth);
        }
        return auth;
    }

    public void invalidate(String appId, String user) {
        cookies.remove(key(appId, user));
    }
    
    public Authenticator newAuthenticator(final String appId,
                                          final String user,
                                          final String pass) {
        return new Authenticator() {
            public RawAuth authenticate() throws AuthenticationException, IOException {
                return RawAuthManager.this.authenticate(appId, user, pass);
            }
            public void invalidate() {
                RawAuthManager.this.invalidate(appId, user);
            }
        };
    }

    public TokenStore getTokenStore() {
        return store;
    }
    
    private String key(String appId, String user) {
        return appId + " " + user;
    }

    public String toString() {
        return String.format("{cookies=%d,tokens=%d}", cookies.size(), store.size());
    }
}

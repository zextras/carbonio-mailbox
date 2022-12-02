// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.auth;

import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslClientFactory;

public class OAuth2SaslClientFactory implements SaslClientFactory {

    public SaslClient createSaslClient(String[] mechanisms, String authorizationId,
        String protocol, String serverName, Map<String, ?> props, CallbackHandler callbackHandler) {
        boolean matchedMechanism = false;
        for (int i = 0; i < mechanisms.length; ++i) {
            if (SaslAuthenticator.XOAUTH2.equalsIgnoreCase(mechanisms[i])) {
                matchedMechanism = true;
                break;
            }
        }
        if (!matchedMechanism) {
            return null;
        }
        return new OAuth2SaslClient((String) props.get("mail." + protocol
            + ".sasl.mechanisms.oauth2.oauthToken"), callbackHandler);
    }

    public String[] getMechanismNames(Map<String, ?> props) {
        return new String[] { SaslAuthenticator.XOAUTH2 };
    }
}

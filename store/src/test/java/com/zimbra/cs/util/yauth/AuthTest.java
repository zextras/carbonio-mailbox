// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.yauth;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import org.apache.log4j.BasicConfigurator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.apache.log4j.Level;

public class AuthTest {
    private static final String APPID = "D2hTUBHAkY0IEL5MA7ibTS_1K86E8RErSSaTGn4-";
    private static final String USER = "dacztest";
    private static final String PASS = "test1234";
    
    private static String token;

    static {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }
    
    private static String getToken() throws Exception {
        if (token == null) {
            token = RawAuth.getToken(APPID, USER, PASS);
        }
        return token;
    }

 @Test
 public void testToken() throws Exception {
        token = getToken();
        assertNotNull(token);
    }

 @Test
 public void testAuthenticate() throws Exception {
        RawAuth auth = RawAuth.authenticate(APPID, getToken());
        assertNotNull(auth.getWSSID());
        assertNotNull(auth.getCookie());
    }

 @Test
 public void testInvalidPassword() throws Exception {
        Exception error = null;
        try {
            RawAuth.getToken(APPID, USER, "invalid");
        } catch (AuthenticationException e) {
            error = e;
        }
        assertNotNull(error);
    }
}

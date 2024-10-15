// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3
package com.zimbra.cs.pgp;

import com.zimbra.cs.signature.SignatureHandler;


public abstract class PgpHandler implements SignatureHandler {

    private static PgpHandler instance = null;

    public static void registerHandler(PgpHandler handler) {
        instance = handler;
    }

    public static PgpHandler getHandler() {
        return instance;
    }

}


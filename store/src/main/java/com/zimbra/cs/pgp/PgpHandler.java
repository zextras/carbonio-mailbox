// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.pgp;

import com.zextras.mailbox.encryption.EncryptionHandler;
import com.zimbra.cs.signature.SignatureHandler;


public abstract class PgpHandler implements SignatureHandler, EncryptionHandler {

    private static PgpHandler instance = null;

    public static void registerHandler(PgpHandler handler) {
        instance = handler;
    }

    public static PgpHandler getHandler() {
        return instance;
    }

}


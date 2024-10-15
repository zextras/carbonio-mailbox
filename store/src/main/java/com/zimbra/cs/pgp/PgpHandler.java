// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3
package com.zimbra.cs.pgp;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.signature.SignatureHandler;

import javax.mail.internet.MimeMessage;
import java.util.List;

public abstract class PgpHandler implements SignatureHandler {

    private static PgpHandler instance = null;

    public static void registerHandler(PgpHandler handler) {
        instance = handler;
    }

    public static PgpHandler getHandler() {
        return instance;
    }

}


// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3
package com.zimbra.cs.smime;

import java.util.List;

import javax.mail.internet.MimeMessage;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;

public abstract class SmimeHandler {

    private static SmimeHandler instance = null;

    public static void registerHandler(SmimeHandler handler) {
        instance = handler;
    }

    public static SmimeHandler getHandler() {
        return instance;
    }

    public abstract boolean verifyMessageSignature(Message msg, Element m, MimeMessage mm,
            OperationContext octxt);

    public abstract MimeMessage decryptMessage(Mailbox mailbox, MimeMessage mime, int itemId) throws ServiceException;

    public abstract void updateCryptoFlags(Message msg, Element m,
        MimeMessage originalMimeMessage, MimeMessage decryptedMimeMessage);

    public abstract MimeMessage decodePKCS7Message(Account account, MimeMessage pkcs7MimeMessage);

    public abstract void addPKCS7SignedMessageSignatureDetails(Account account, Element m,
        MimeMessage mm, SoapProtocol mResponseProtocol);

    public abstract void encodeCertificate(Account account, Element elem, String certData,
            SoapProtocol mResponseProtocol, List<String> emailAddresses);
}


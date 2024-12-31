// SPDX-FileCopyrightText: 2022 Synacor, Inc.
//
// SPDX-License-Identifier: Zimbra-1.3
package com.zimbra.cs.smime;

import java.util.List;

import javax.mail.internet.MimeMessage;

import com.zextras.mailbox.encryption.smime.SmimeHandlerImpl;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zextras.mailbox.encryption.EncryptionHandler;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.signature.SignatureHandler;

public abstract class SmimeHandler implements SignatureHandler, EncryptionHandler {

    private static SmimeHandler instance = null;

    public static void registerHandler(SmimeHandler handler) {
        instance = handler;
    }

    public static SmimeHandler getHandler() {

        if (instance != null) {
            return instance;
        }

        synchronized (SmimeHandler.class) {
            if (instance == null) {
                instance = new SmimeHandlerImpl();
            }
        }

        return instance;
    }

    public abstract void updateCryptoFlags(Message msg, Element m,
        MimeMessage originalMimeMessage, MimeMessage decryptedMimeMessage);

    public abstract MimeMessage decodePKCS7Message(Account account, MimeMessage pkcs7MimeMessage);


    public abstract void encodeCertificate(Account account, Element elem, String certData,
            SoapProtocol mResponseProtocol, List<String> emailAddresses);
}


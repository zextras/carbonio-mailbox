// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.signature;

import com.zimbra.common.soap.Element;

import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;

import javax.mail.internet.MimeMessage;


public interface SignatureHandler {
    boolean verifyMessageSignature(Message msg, Element m, MimeMessage mm, OperationContext octxt);

    default void verifyMessageSignature(Element m, MimeMessage mm) {
        verifyMessageSignature(null, m, mm, null);
    }

    boolean signatureEnabled();
}

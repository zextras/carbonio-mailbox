package com.zimbra.cs.signature;

import com.zimbra.common.soap.Element;

import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;

import javax.mail.internet.MimeMessage;


public interface SignatureHandler {
    boolean verifyMessageSignature(Message msg, Element m, MimeMessage mm, OperationContext octxt);

    default boolean verifyMessageSignature(Element m, MimeMessage mm) {
        return verifyMessageSignature(null, m, mm, null);
    }
}

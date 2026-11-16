// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;


import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPTransport;

public class MailUtil {
    
    /* 
     * This is a wrapper around SMTPTransport meant for validating the recipient addresses.
     * This is helpful for capturing the addresses with 550 smtp error code.
     * The sendMessage() and send() methods will not send the message and always throws MessagingException.
     * The callsite can check to see if MessagingException is an instance of SendFailedException
     * and then get the list of invalid e-mail addresses. 
     */
    
    private static class VerifyRcptSMTPTransport extends SMTPTransport {
        public VerifyRcptSMTPTransport(Session session, URLName urlname) {
            super(session, urlname);
        }

        @Override
        protected void rcptTo() throws MessagingException {
            super.rcptTo();
            // always throw the exception.
            throw new MessagingException();
        }
    }
    
    public static void validateRcptAddresses(Session session, Address[] addresses) throws MessagingException{
        VerifyRcptSMTPTransport.send(new MimeMessage(session), addresses);     
    }    
}
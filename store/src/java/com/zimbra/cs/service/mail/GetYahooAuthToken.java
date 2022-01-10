// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.util.yauth.MetadataTokenStore;
import com.zimbra.cs.util.yauth.TokenAuthenticateV1;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * 
 */
public class GetYahooAuthToken extends MailDocumentHandler {
    
    private static final String APPID = "ZYMSGRINTEGRATION";

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Mailbox mbox = getRequestedMailbox(zsc);
        
        MetadataTokenStore mts = new MetadataTokenStore(mbox);
        
        String userId = request.getAttribute("user");
        String passwd = request.getAttribute("password");
        
        Element response = zsc.createElement(MailConstants.GET_YAHOO_AUTH_TOKEN_RESPONSE);
        
        try {
            String token = TokenAuthenticateV1.getToken(userId, passwd);
            mts.putToken(APPID, userId, token);
            if (token == null) {
                response.addAttribute("failed", true);
            }
        } catch (IOException | HttpException e) {
            throw ServiceException.FAILURE("IOException", e);
        } 
        
        
        return response;
    }
}

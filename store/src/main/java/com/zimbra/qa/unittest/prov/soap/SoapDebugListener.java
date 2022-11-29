// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.soap;

import java.net.URI;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapHttpTransport.HttpDebugListener;

public class SoapDebugListener implements HttpDebugListener {
    
    enum Level {
        OFF,
        HEADER,
        BODY,
        ALL;
        
        private static boolean needsHeader(Level level) {
            return level == Level.ALL || level == Level.HEADER;
        }
        
        private static boolean needsBody(Level level) {
            return level == Level.ALL || level == Level.BODY;
        }
    }
    
    private Level level = Level.BODY;
    
    SoapDebugListener() {
    }
    
    SoapDebugListener(Level level) {
        this.level = level;
    }
    
    @Override
    public void receiveSoapMessage(HttpPost postMethod, Element envelope) {
        if (level == Level.OFF) {
            return;
        }
        
        System.out.println();
        System.out.println("=== Response ===");
        
        if (Level.needsHeader(level)) {
            Header[] headers = postMethod.getAllHeaders();
            for (Header header : headers) {
                System.out.println(header.toString().trim()); // trim the ending crlf
            }
            System.out.println();
        }
        
        if (Level.needsBody(level)) {
            System.out.println(envelope.prettyPrint());
        }
    }

    @Override
    public void sendSoapMessage(HttpPost postMethod, Element envelope, BasicCookieStore httpState) {
        if (level == Level.OFF) {
            return;
        }
        
        System.out.println();
        System.out.println("=== Request ===");
        
        if (Level.needsHeader(level)) {
            
            URI uri = postMethod.getURI();
            System.out.println(uri.toString());
            
            
            // headers
            Header[] headers = postMethod.getAllHeaders();
            for (Header header : headers) {
                System.out.println(header.toString().trim()); // trim the ending crlf
            }
            System.out.println();
            
            //cookies
            if (httpState != null) {
                List<Cookie> cookies = httpState.getCookies();
                for (Cookie cookie : cookies) {
                    System.out.println("Cookie: " + cookie.toString());
                }
            }
            System.out.println();
        }
        
        if (Level.needsBody(level)) {
            System.out.println(envelope.prettyPrint());
        }
    }
}

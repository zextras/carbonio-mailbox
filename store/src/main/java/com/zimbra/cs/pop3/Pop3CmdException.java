// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Nov 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.zimbra.cs.pop3;

/**
 * @author schemers
 */
public class Pop3CmdException extends Exception {
    
    public Pop3CmdException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public Pop3CmdException(String msg) {
        super(msg);
    }
    
    /**
     * sanitize the result of getMessage for use in an -ERR response
     * 
     * @return
     */
    public String getResponse() {
        return getResponse(getMessage());
    }
    
    /**
     * sanitize the specified message for use in an -ERR response
     * 
     * @return
     */
    public static String getResponse(String msg) {
        String resp = msg;
        if (resp.length() > Pop3Handler.MAX_RESPONSE_TEXT)
            resp = resp.substring(0, Pop3Handler.MAX_RESPONSE_TEXT);
        if (resp.indexOf('\r') != -1)
            resp = resp.replaceAll("\r", " ");
        if (resp.indexOf('\n') != -1)
            resp = resp.replaceAll("\n", " ");        
        return resp;
    }
}

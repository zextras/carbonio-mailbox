// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

/**
 * An exception that can occur due to client-side errors.  
 */
public class LmcSoapClientException extends Exception {
    
    public LmcSoapClientException(String message) {
        super(message);
    }
}
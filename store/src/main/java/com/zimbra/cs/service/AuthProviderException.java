// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import com.zimbra.common.service.ServiceException;

@SuppressWarnings("serial")
public class AuthProviderException extends ServiceException {
    
    private boolean mCanIgnore;
    
    // auth data is not present for the auth provider
    public static final String NO_AUTH_DATA        = "authprovider.NO_AUTH_DATA";
    
    // auth method is not supported by the auth provider
    public static final String NOT_SUPPORTED       = "authprovider.NOT_SUPPORTED";
    
    // internal auth provider error
    public static final String FAILURE             = "authprovider.FAILURE";
    
    private AuthProviderException(String message, String code, boolean isReceiversFault) {
        super(message, code, isReceiversFault);
        setCanIgnore(true);
    }
    
    private AuthProviderException(String message, String code, boolean isReceiversFault, Throwable cause) {
        super(message, code, isReceiversFault, cause);
        setCanIgnore(true);
    }
    
    private void setCanIgnore(boolean canIgnore) {
        mCanIgnore = canIgnore;
    }
    
    public boolean canIgnore() {
        return mCanIgnore;
    }
    
    public static AuthProviderException NO_AUTH_DATA() {
        return new AuthProviderException("no auth token", NO_AUTH_DATA, SENDERS_FAULT, null);
    }
    
    public static AuthProviderException NOT_SUPPORTED() {
        return new AuthProviderException("not suported", NOT_SUPPORTED, SENDERS_FAULT, null);
    }
    
    public static AuthProviderException FAILURE(String message) {
        return new AuthProviderException("failure:" + message, FAILURE, SENDERS_FAULT, null);
    }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;

/**
 * 
 */
@SuppressWarnings("serial")
public class AdminServiceException extends ServiceException {
    public static final String NO_SUCH_WAITSET = "admin.NO_SUCH_WAITSET";
    
    private AdminServiceException(String message, String code, boolean isReceiversFault, Throwable cause, Argument... args) {
        super(message, code, isReceiversFault, cause, args);
    }
    
    public static AdminServiceException NO_SUCH_WAITSET(String id) {
        return new AdminServiceException("No such waitset: "+id, NO_SUCH_WAITSET, SENDERS_FAULT,
            null, new Argument("id", id, Argument.Type.STR));
    }
    
}

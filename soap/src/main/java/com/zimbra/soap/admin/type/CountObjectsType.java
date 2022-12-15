// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.type;

import com.google.common.base.Joiner;
import com.zimbra.common.service.ServiceException;

public enum CountObjectsType {
    userAccount(true, false),
    account(true, false),
    alias(true, false),
    dl(true, false),
    domain(true, false),
    cos(false, false),
    server(false, false),
    calresource(true, false),

    // UC service objects
    accountOnUCService(false, true),
    cosOnUCService(false, true),
    domainOnUCService(false, true),
    
    // for license counting
    internalUserAccount(true, false),
    internalArchivingAccount(true, false),
    internalUserAccountX(true, false);
    
    private boolean allowsDomain;
    private boolean allowsUCService;
    
    private CountObjectsType(boolean allowsDomain, boolean allowsUCService) {
        this.allowsDomain = allowsDomain;
        this.allowsUCService = allowsUCService;
    }

    public static CountObjectsType fromString(String type) throws ServiceException {
        try {
            // for backward compatibility, installer uses userAccounts
            if ("userAccounts".equals(type)) {
                return userAccount;
            } else {
                return CountObjectsType.valueOf(type);
            }
        } catch (IllegalArgumentException e) {
            throw ServiceException.INVALID_REQUEST("unknown count objects type: " + type, e);
        }
    }

    public static String names(String separator) {
        Joiner joiner = Joiner.on(separator);
        return joiner.join(CountObjectsType.values());
    }
    
    public boolean allowsDomain() {
        return allowsDomain;
    }
    
    public boolean allowsUCService() {
        return allowsUCService;
    }

}

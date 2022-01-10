// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.account.message.ChangePasswordResponse;

public class ZChangePasswordResult {
    private ZAuthToken mAuthToken;
    private long mExpires;
    private long mLifetime;

    public ZChangePasswordResult(Element e) throws ServiceException {
        String authToken = e.getAttribute(AccountConstants.E_AUTH_TOKEN);
        mAuthToken = new ZAuthToken(null, authToken, null);

        mLifetime = e.getAttributeLong(AccountConstants.E_LIFETIME);
        mExpires = System.currentTimeMillis() + mLifetime;
    }
    
    public ZChangePasswordResult(ChangePasswordResponse res) {
        mAuthToken = new ZAuthToken(null, res.getAuthToken());
        mLifetime = res.getLifetime();
        mExpires = System.currentTimeMillis() + mLifetime;
    }

    public ZAuthToken getAuthToken() {
        return mAuthToken;
    }

    public long getExpires() {
        return mExpires;
    }

    public long getLifetime() {
        return mLifetime;
    }
}
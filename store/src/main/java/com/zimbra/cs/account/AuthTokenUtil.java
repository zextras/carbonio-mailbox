// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;

public class AuthTokenUtil {

    public static void encodeAuthResp(AuthToken at, Element parent, boolean isAdmin)  throws ServiceException {
        parent.addNonUniqueElement(isAdmin ? AdminConstants.E_AUTH_TOKEN : AccountConstants.E_AUTH_TOKEN).setText(getOrigAuthData(at));
    }

    public static String getOrigAuthData(AuthToken at) throws ServiceException {
        String origAuthData = null;
        try {
            origAuthData = at.getEncoded();
            if (origAuthData == null) {
                throw ServiceException.FAILURE("unable to get encoded auth token", null);
            }
        } catch (AuthTokenException e) {
            throw ServiceException.FAILURE("unable to get encoded auth token", e);
        }
        return origAuthData;
    }

    public static boolean isZimbraUser(String type) {
        return StringUtil.isNullOrEmpty(type) || AuthTokenProperties.C_TYPE_ZIMBRA_USER.equals(type) || AuthTokenProperties.C_TYPE_ZMG_APP.equals(type);
    }

    public static AuthTokenKey getCurrentKey() throws AuthTokenException {
        try {
            AuthTokenKey key = AuthTokenKey.getCurrentKey();
            return key;
        } catch (ServiceException e) {
            ZimbraLog.account.error("unable to get latest AuthTokenKey", e);
            throw new AuthTokenException("unable to get AuthTokenKey", e);
        }
    }
}

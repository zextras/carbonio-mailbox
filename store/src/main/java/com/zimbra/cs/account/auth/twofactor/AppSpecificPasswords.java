// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth.twofactor;

import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AppSpecificPassword;

public interface AppSpecificPasswords extends SecondFactor {
    boolean isEnabled() throws ServiceException;
    AppSpecificPassword generatePassword(String name) throws ServiceException;
    Set<AppSpecificPasswordData> getPasswords() throws ServiceException;
    void revoke(String appName) throws ServiceException;
    void revokeAll() throws ServiceException;
    String getAppNameByPassword(String password) throws ServiceException;
}

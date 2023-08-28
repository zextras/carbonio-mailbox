// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth.twofactor;

import java.util.Set;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AppSpecificPassword;

public interface AppSpecificPasswords extends SecondFactor {
    public boolean isEnabled() throws ServiceException;
    public AppSpecificPassword generatePassword(String name) throws ServiceException;
    public Set<AppSpecificPasswordData> getPasswords() throws ServiceException;
    public void revoke(String appName) throws ServiceException;
    public void revokeAll() throws ServiceException;
    public String getAppNameByPassword(String password) throws ServiceException;
}

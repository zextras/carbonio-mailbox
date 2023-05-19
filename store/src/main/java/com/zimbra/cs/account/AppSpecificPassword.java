// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.auth.twofactor.AppSpecificPasswordData;

public interface AppSpecificPassword {
    void store() throws ServiceException;

    void update() throws ServiceException;

    String getName();

    String getPassword();

    void setDateLastUsed(Long date);

    void setDateCreated(Long date);

    Long getDateLastUsed();

    Long getDateCreated();

    boolean validate(String providedPassword) throws ServiceException;

    void revoke() throws ServiceException;

    AppSpecificPasswordData getPasswordData();

    boolean isExpired();
}

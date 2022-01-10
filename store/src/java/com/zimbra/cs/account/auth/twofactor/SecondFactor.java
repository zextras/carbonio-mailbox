// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth.twofactor;

import com.zimbra.common.service.ServiceException;

public interface SecondFactor {

    public void authenticate(String secondFactor) throws ServiceException;
}

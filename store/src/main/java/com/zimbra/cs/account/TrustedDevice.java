// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Map;

import com.zimbra.common.service.ServiceException;

public interface TrustedDevice {

    void register() throws ServiceException;

    void revoke() throws ServiceException;

    boolean verify(Map<String, Object> attrs);

    Map<String, Object> getAttrs();

    TrustedDeviceToken getToken();

    Integer getTokenId();

    long getExpires();

    boolean isExpired();

}

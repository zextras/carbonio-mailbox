// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Map;

import com.zimbra.common.service.ServiceException;

public interface TrustedDevice {

    public void register() throws ServiceException;

    public void revoke() throws ServiceException;

    public boolean verify(Map<String, Object> attrs);

    public Map<String, Object> getAttrs();

    public TrustedDeviceToken getToken();

    public Integer getTokenId();

    public long getExpires();

    public boolean isExpired();

}

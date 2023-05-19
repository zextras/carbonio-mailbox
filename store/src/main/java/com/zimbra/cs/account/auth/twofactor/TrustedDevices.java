// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth.twofactor;

import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.TrustedDevice;
import com.zimbra.cs.account.TrustedDeviceToken;

public interface TrustedDevices {
    TrustedDeviceToken registerTrustedDevice(Map<String, Object> deviceAttrs) throws ServiceException;
    List<TrustedDevice> getTrustedDevices() throws ServiceException;
    void revokeTrustedDevice(TrustedDeviceToken token) throws ServiceException;
    void revokeAllTrustedDevices() throws ServiceException;
    void revokeOtherTrustedDevices(TrustedDeviceToken token) throws ServiceException;
    void verifyTrustedDevice(TrustedDeviceToken token, Map<String, Object> attrs) throws ServiceException;
    TrustedDeviceToken getTokenFromRequest(Element request, Map<String, Object> context) throws ServiceException;
    TrustedDevice getTrustedDeviceByTrustedToken(TrustedDeviceToken token) throws ServiceException;
}

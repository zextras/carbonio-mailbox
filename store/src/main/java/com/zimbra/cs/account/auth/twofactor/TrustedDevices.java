// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth.twofactor;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.TrustedDevice;
import com.zimbra.cs.account.TrustedDeviceToken;
import java.util.List;
import java.util.Map;

public interface TrustedDevices {
  public TrustedDeviceToken registerTrustedDevice(Map<String, Object> deviceAttrs)
      throws ServiceException;

  public List<TrustedDevice> getTrustedDevices() throws ServiceException;

  public void revokeTrustedDevice(TrustedDeviceToken token) throws ServiceException;

  public void revokeAllTrustedDevices() throws ServiceException;

  public void revokeOtherTrustedDevices(TrustedDeviceToken token) throws ServiceException;

  public void verifyTrustedDevice(TrustedDeviceToken token, Map<String, Object> attrs)
      throws ServiceException;

  public TrustedDeviceToken getTokenFromRequest(Element request, Map<String, Object> context)
      throws ServiceException;

  public TrustedDevice getTrustedDeviceByTrustedToken(TrustedDeviceToken token)
      throws ServiceException;
}

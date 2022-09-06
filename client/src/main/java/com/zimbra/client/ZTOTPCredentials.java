// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.common.service.ServiceException;
import com.zimbra.soap.account.message.TwoFactorCredentials;
import java.util.HashSet;
import java.util.Set;

public class ZTOTPCredentials {
  private String secret;
  private Set<String> scratchCodes;

  public ZTOTPCredentials(TwoFactorCredentials twoFactorCredentials) throws ServiceException {
    secret = twoFactorCredentials.getSharedSecret();
    scratchCodes = new HashSet<String>();
    for (String code : twoFactorCredentials.getScratchCodes()) {
      scratchCodes.add(code);
    }
  }

  public String getSecret() {
    return secret;
  }

  public Set<String> getScratchCodes() {
    return scratchCodes;
  }
}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth.twofactor;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.auth.twofactor.TwoFactorAuth.CredentialConfig;
import java.util.List;

public interface ScratchCodes extends SecondFactor {
  public List<String> getCodes();

  public List<String> generateCodes(CredentialConfig config) throws ServiceException;

  public void storeCodes(List<String> codes) throws ServiceException;
}

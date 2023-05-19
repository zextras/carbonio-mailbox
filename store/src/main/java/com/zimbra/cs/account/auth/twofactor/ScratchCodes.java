// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.auth.twofactor;

import java.util.List;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.auth.twofactor.TwoFactorAuth.CredentialConfig;

public interface ScratchCodes extends SecondFactor {
    List<String> getCodes();
    List<String> generateCodes(CredentialConfig config) throws ServiceException;
    void storeCodes(List<String> codes) throws ServiceException;
}


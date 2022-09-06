// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.soap;

import com.zimbra.common.service.ServiceException;
import java.util.Map;

interface SoapEntry {

  public void modifyAttrs(
      SoapProvisioning prov, Map<String, ? extends Object> attrs, boolean checkImmutable)
      throws ServiceException;

  public void reload(SoapProvisioning prov) throws ServiceException;
}

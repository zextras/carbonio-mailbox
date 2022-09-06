// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.cache;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mime.MimeTypeInfo;
import java.util.List;

public interface IMimeTypeCache {
  public void flushCache(Provisioning prov) throws ServiceException;

  public List<MimeTypeInfo> getAllMimeTypes(Provisioning prov) throws ServiceException;

  public List<MimeTypeInfo> getMimeTypes(Provisioning prov, String mimeType)
      throws ServiceException;
}

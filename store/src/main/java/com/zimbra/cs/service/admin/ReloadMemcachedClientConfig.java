// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.memcached.MemcachedConnector;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public class ReloadMemcachedClientConfig extends AdminDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraLog.misc.info("Reloading memcached client configuration");
    MemcachedConnector.reloadConfig();
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Element response = zsc.createElement(AdminConstants.RELOAD_MEMCACHED_CLIENT_CONFIG_RESPONSE);
    return response;
  }
}

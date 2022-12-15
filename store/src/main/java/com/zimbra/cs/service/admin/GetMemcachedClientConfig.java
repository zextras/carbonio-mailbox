// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.memcached.ZimbraMemcachedClient;
import com.zimbra.cs.memcached.MemcachedConnector;
import com.zimbra.soap.ZimbraSoapContext;

public class GetMemcachedClientConfig extends AdminDocumentHandler {

    @Override public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Element response = zsc.createElement(AdminConstants.GET_MEMCACHED_CLIENT_CONFIG_RESPONSE);
        ZimbraMemcachedClient zmcd = MemcachedConnector.getClient();
        if (zmcd != null) {
            response.addAttribute(AdminConstants.A_MEMCACHED_CLIENT_CONFIG_SERVER_LIST, zmcd.getServerList());
            response.addAttribute(AdminConstants.A_MEMCACHED_CLIENT_CONFIG_HASH_ALGORITHM, zmcd.getHashAlgorithm());
            response.addAttribute(AdminConstants.A_MEMCACHED_CLIENT_CONFIG_BINARY_PROTOCOL, zmcd.getBinaryProtocolEnabled());
            response.addAttribute(AdminConstants.A_MEMCACHED_CLIENT_CONFIG_DEFAULT_EXPIRY_SECONDS, zmcd.getDefaultExpirySeconds());
            response.addAttribute(AdminConstants.A_MEMCACHED_CLIENT_CONFIG_DEFAULT_TIMEOUT_MILLIS, zmcd.getDefaultTimeoutMillis());
        }
        return response;
    }
}

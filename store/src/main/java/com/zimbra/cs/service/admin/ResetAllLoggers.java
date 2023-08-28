// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.LogFactory;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * Removes all account loggers and reloads {@code /opt/zextras/conf/log4j.properties}.
 *
 * @author ysasaki
 */
public final class ResetAllLoggers extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        LogFactory.reset();
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        Element response = zsc.createElement(AdminConstants.RESET_ALL_LOGGERS_RESPONSE);
        return response;
    }

}

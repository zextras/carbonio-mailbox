// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;


public class MailMode extends LocalBind {
    static final String ERROR_MM_W_IP_SEC = "Only \"https\" and \"both\" are valid modes when requiring interprocess security with web proxy.";
    static final String ERROR_MM_WO_IP_SEC = "Only \"http\" and \"both\" are valid modes when not requiring interprocess security with web proxy.";
    @Override
    public void preModify(CallbackContext context, String attrName, Object attrValue,
            Map attrsToModify, Entry entry)
    throws ServiceException {
        SingleValueMod mod = singleValueMod(attrsToModify, attrName);
        if (mod.setting()) {
            String zimbraMailMode = mod.value();
            if (isReverseProxySSLToUpstreamEnabled(entry)) {
                if (!(zimbraMailMode.equals("https") || zimbraMailMode.equals("both"))) {
                    throw ServiceException.INVALID_REQUEST(ERROR_MM_W_IP_SEC, null);
                }
            }
            else if (!(zimbraMailMode.equals("http") || zimbraMailMode.equals("both"))) {
                throw ServiceException.INVALID_REQUEST(ERROR_MM_WO_IP_SEC, null);
            }
        }
    }

    private boolean isReverseProxySSLToUpstreamEnabled (Entry entry)
    throws ServiceException {
        if (entry instanceof Server) {
            Server server = (Server) entry;
            return server.isReverseProxySSLToUpstreamEnabled();
        }
        else {
            return Provisioning.getInstance().getConfig().isReverseProxySSLToUpstreamEnabled();
        }
    }
}

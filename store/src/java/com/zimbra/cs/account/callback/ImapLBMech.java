// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.imap.ImapLoadBalancingMechanism;

public class ImapLBMech extends AttributeCallback {

    @Override
    public void preModify(CallbackContext context, String attrName, Object attrValue, Map attrsToModify, Entry entry)
    throws ServiceException {	

        String lbMech;

        SingleValueMod mod = singleValueMod(attrName, attrValue);
        if (mod.setting()) {
            lbMech = mod.value();

            boolean valid = false;

            if (lbMech == null) {
                valid = true;
            } else if (lbMech.startsWith(ImapLoadBalancingMechanism.ImapLBMech.custom.name())) {
            	valid = true;
            } else {
                try {
                	/* will raise exception if invalid string */
                	ImapLoadBalancingMechanism.ImapLBMech.fromString(lbMech);
                	valid = true;
                } catch (ServiceException e) {
                    ZimbraLog.account.error("invalid IMAP load balancing mechanism", e);
                }            	
            }

            if (!valid) {
                throw ServiceException.INVALID_REQUEST("invalid value: " + lbMech, null);
            }
        }
    }


    @Override
    public void postModify(CallbackContext context, String attrName, Entry entry) {
    }

}

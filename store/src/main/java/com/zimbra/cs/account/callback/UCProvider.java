// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;

public class UCProvider extends AttributeCallback {
    @Override
    public void preModify(CallbackContext context, String attrName,
            Object attrValue, Map attrsToModify, Entry entry)
    throws ServiceException {
        SingleValueMod mod = singleValueMod(attrsToModify, attrName);
        if (mod.unsetting()) {
            return;
        }

        String newValue = mod.value();
        String allowedValue = Provisioning.getInstance().getConfig().getUCProviderEnabled();

        if (allowedValue == null) {
            throw ServiceException.INVALID_REQUEST("no " + Provisioning.A_zimbraUCProviderEnabled +
                    " is configured on global config",  null);
        }

        if (!allowedValue.equals(newValue)) {
            throw ServiceException.INVALID_REQUEST("UC provider " + newValue + " is not allowed " +
                    " by " + Provisioning.A_zimbraUCProviderEnabled, null);
        }

    }

    @Override
    public void postModify(CallbackContext context, String attrName, Entry entry) {
    }

}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;

public class AutoProvLdapSearchFilter extends AttributeCallback {

    @Override
    public void preModify(CallbackContext context, String attrName, Object attrValue, Map attrsToModify, Entry entry)
            throws ServiceException {
        //reset zimbraAutoProvLastPolledTimestamp
        String oldValue = null;
        String newValue = null;
        if (entry != null) {
            oldValue = entry.getAttr(attrName, true);
        }
        SingleValueMod mod = singleValueMod(attrsToModify, attrName);
        newValue = mod.value();
        if (!StringUtil.equal(oldValue, newValue)) {
            attrsToModify.put(Provisioning.A_zimbraAutoProvLastPolledTimestamp, null);
        }
    }

    @Override
    public void postModify(CallbackContext context, String attrName, Entry entry) {
    }

}

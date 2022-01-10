// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.DynamicGroup;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;

public class MemberURL extends AttributeCallback {

    @Override
    public void preModify(CallbackContext context, String attrName, Object attrValue,
            Map attrsToModify, Entry entry)
    throws ServiceException {

        if (context.isDoneAndSetIfNot(MemberURL.class)) {
            return;
        }

        if (context.isCreate()) {
            // memberURL can be set to anything during create
            // zimbraIsACLGroup will be checked in createDynamicGroup
            return;
        }

        // not creating, ensure zimbraIsACLGroup must be FALSE
        boolean isACLGroup = entry.getBooleanAttr(Provisioning.A_zimbraIsACLGroup, true);

        if (isACLGroup) {
            throw ServiceException.INVALID_REQUEST("cannot modify " + Provisioning.A_memberURL +
                    " when " +  Provisioning.A_zimbraIsACLGroup + " is TRUE", null);
        }

    }

    @Override
    public void postModify(CallbackContext context, String attrName, Entry entry) {
    }
}

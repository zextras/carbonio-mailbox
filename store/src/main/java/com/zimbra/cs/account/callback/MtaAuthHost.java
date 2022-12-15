// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;

public class MtaAuthHost extends AttributeCallback {

    /**
     * check to make sure zimbraMtaAuthHost points to a valid server zimbraServiceHostname
     */
    @Override
    public void preModify(CallbackContext context, String attrName, Object value,
            Map attrsToModify, Entry entry) 
    throws ServiceException {
        //do nothing this attribute has been deprecated
    }


    @Override
    public void postModify(CallbackContext context, String attrName, Entry entry) {
    }
}

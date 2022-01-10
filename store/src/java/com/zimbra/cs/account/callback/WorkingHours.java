// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.callback;

import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.Entry;

public class WorkingHours extends AttributeCallback {

    // Value must be a comma-separated string whose parts are colon-separated strings.
    // Each comma-separated part specifies the working hours of a day of the week.
    // Each day of the week must be specified exactly once.
    // 
    @Override
    public void preModify(CallbackContext context, String attrName, Object attrValue, 
            Map attrsToModify, Entry entry)
    throws ServiceException {
        if (attrValue == null) {
            return;  // Allow unsetting.
        }
        if (!(attrValue instanceof String)) {
            throw ServiceException.INVALID_REQUEST(attrValue + " is a single-valued string", null);
        }
        String value = (String) attrValue;
        if (value.length() == 0) {
            return;  // Allow unsetting.
        }
        com.zimbra.cs.fb.WorkingHours.validateWorkingHoursPref(value);
    }

    @Override
    public void postModify(CallbackContext context, String attrName, Entry entry) {
    }
}

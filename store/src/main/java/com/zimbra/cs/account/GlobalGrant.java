// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import java.util.Map;

public class GlobalGrant extends Entry {
    
    public GlobalGrant(Map<String, Object> attrs, Provisioning provisioning) {
        super(attrs, null, provisioning);
        resetData();
    }
    
    @Override
    public EntryType getEntryType() {
        return EntryType.GLOBALGRANT;
    }
    
    @Override
    public String getLabel() {
        return "globalacltarget";
    }
}

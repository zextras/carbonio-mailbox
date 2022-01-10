// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.gal;

public class GalFilter {
    static final String DEFAULT_SYNC_FILTER = 
        "(&(|(objectclass=zimbraAccount)(objectclass=zimbraDistributionList)(objectclass=zimbraGroup))(!(&(objectclass=zimbraCalendarResource)(!(zimbraAccountStatus=active)))))";

    static enum NamedFilter {
        zimbraAccounts,
        zimbraResources,
        zimbraGroups,
        
        zimbraAccountAutoComplete,
        zimbraResourceAutoComplete,
        zimbraGroupAutoComplete,
        
        zimbraAccountSync,
        zimbraResourceSync,
        zimbraGroupSync,
        
        zimbraAutoComplete,
        zimbraSearch,
        zimbraSync;
    };
    
}

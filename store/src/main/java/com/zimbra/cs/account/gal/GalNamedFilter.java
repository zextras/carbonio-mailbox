// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.gal;

public class GalNamedFilter {
    /*
     * GAL autocomplete
     */
    private static final String ZIMBRA_ACCOUNT_AUTO_COMPLETE = "zimbraAccountAutoComplete";
    private static final String ZIMBRA_CALENDAR_RESOURCE_AUTO_COMPLETE = "zimbraResourceAutoComplete"; 
    private static final String ZIMBRA_GROUP_AUTO_COMPLETE = "zimbraGroupAutoComplete"; 
    
    /*
     * GAL search
     */
    private static final String ZIMBRA_ACCOUNTS = "zimbraAccounts";
    private static final String ZIMBRA_CALENDAR_RESOURCES = "zimbraResources";
    private static final String ZIMBRA_GROUPS = "zimbraGroups";
    
    /*
     * GAL sync
     */
    private static final String ZIMBRA_ACCOUNT_SYNC = "zimbraAccountSync";
    private static final String ZIMBRA_CALENDAR_RESOURCE_SYNC = "zimbraResourceSync"; 
    private static final String ZIMBRA_GROUP_SYNC = "zimbraGroupSync"; 
    
    public static String getZimbraCalendarResourceFilter(GalOp galOp) {
        String filter = null;
        
        if (galOp == GalOp.autocomplete)
            filter = GalNamedFilter.ZIMBRA_CALENDAR_RESOURCE_AUTO_COMPLETE;
        else if (galOp == GalOp.search)
            filter = GalNamedFilter.ZIMBRA_CALENDAR_RESOURCES;
        else if (galOp == GalOp.sync)
            filter = GalNamedFilter.ZIMBRA_CALENDAR_RESOURCE_SYNC;
        
        return filter;
    }
    
    public static String getZimbraGroupFilter(GalOp galOp) {
        String filter = null;
        
        if (galOp == GalOp.autocomplete)
            filter = GalNamedFilter.ZIMBRA_GROUP_AUTO_COMPLETE;
        else if (galOp == GalOp.search)
            filter = GalNamedFilter.ZIMBRA_GROUPS;
        else if (galOp == GalOp.sync)
            filter = GalNamedFilter.ZIMBRA_GROUP_SYNC;
        
        return filter;
    }
    
    public static String getZimbraAcountFilter(GalOp galOp) {
        String filter = null;
        
        if (galOp == GalOp.autocomplete)
            filter = GalNamedFilter.ZIMBRA_ACCOUNT_AUTO_COMPLETE;
        else if (galOp == GalOp.search)
            filter = GalNamedFilter.ZIMBRA_ACCOUNTS;
        else if (galOp == GalOp.sync)
            filter = GalNamedFilter.ZIMBRA_ACCOUNT_SYNC;
        
        return filter;
    }
}

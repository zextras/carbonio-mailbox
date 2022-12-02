// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.accesscontrol.generated.AdminRights;

public abstract class AdminRight extends Right {
    //
    // pseudo rights, should never actually be granted on any entry 
    //
    
    public static AttrRight PR_GET_ATTRS;
    public static AttrRight PR_SET_ATTRS;
    
    // pseudo rights to always allow/deny, used by admin soap handlers for 
    // API clean-ness when coding with legacy domain based AccessManager 
    // permission checking code.
    public static AdminRight PR_ALWAYS_ALLOW;
    public static AdminRight PR_SYSTEM_ADMIN_ONLY;
    
    // pseudo right for collecting effective admin preset right grants
    public static AdminRight PR_ADMIN_PRESET_RIGHT;
    
    static void init(RightManager rm) throws ServiceException {
        
        PR_GET_ATTRS = (AttrRight)newAdminSystemRight("PSEUDO_GET_ATTRS", RightType.getAttrs);
        PR_SET_ATTRS = (AttrRight)newAdminSystemRight("PSEUDO_SET_ATTRS", RightType.setAttrs);
        
        PR_ALWAYS_ALLOW = newAdminSystemRight("PSEUDO_ALWAYS_ALLOW", RightType.preset);
        PR_SYSTEM_ADMIN_ONLY = newAdminSystemRight("PSEUDO_SYSTEM_ADMIN_ONLY", RightType.preset);
        PR_ADMIN_PRESET_RIGHT = newAdminSystemRight("PSEUDO_ADMIN_PRESET_RIGHT", RightType.preset);
        
        if (LC.zimbra_rights_delegated_admin_supported.booleanValue())
            AdminRights.init(rm);
    }
    
    protected AdminRight(String name, RightType rightType) {
        super(name, rightType);
    }
    
    static AdminRight newAdminSystemRight(String name, RightType rightType) throws ServiceException {
        return newAdminRight(name, rightType);
    }
    
    private static AdminRight newAdminRight(String name, RightType rightType) throws ServiceException {
        if (rightType == RightType.getAttrs || rightType == RightType.setAttrs)
            return new AttrRight(name, rightType);
        else if (rightType == RightType.combo)
            return new ComboRight(name);
        else
            return new PresetRight(name);
    }
    
    /*
    String dump(StringBuilder sb) {
        // nothing in user right to dump
        return super.dump(sb);
    }
    */
}

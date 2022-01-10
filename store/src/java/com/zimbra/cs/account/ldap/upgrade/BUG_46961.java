// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;

public class BUG_46961 extends UpgradeOp {

    @Override
    void doUpgrade() throws ServiceException {
        Config config = prov.getConfig();
        
        String oldValue = "displayName,cn=fullName";
        String newValue = "displayName,cn=fullName,fullName2,fullName3,fullName4,fullName5,fullName6,fullName7,fullName8,fullName9,fullName10";
        
        String[] curValues = config.getMultiAttr(Provisioning.A_zimbraGalLdapAttrMap);
         
        for (String value : curValues) {
            if (value.equalsIgnoreCase(oldValue)) {
                Map<String, Object> attr = new HashMap<String, Object>();
                attr.put("-" + Provisioning.A_zimbraGalLdapAttrMap, oldValue);
                attr.put("+" + Provisioning.A_zimbraGalLdapAttrMap, newValue);
                
                printer.println("Modifying " + Provisioning.A_zimbraGalLdapAttrMap + " on global config:");
                printer.println("    removing value: " + oldValue);
                printer.println("    adding value: " + newValue);
                prov.modifyAttrs(config, attr);
                
            }
        }
    }

}

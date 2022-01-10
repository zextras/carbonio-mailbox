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

public class BUG_41000 extends UpgradeOp {

    @Override
    void doUpgrade() throws ServiceException {
        Config config = prov.getConfig();
        
        String[] value = 
        {
         "zimbraAutoComplete:(&(|(displayName=%s*)(cn=%s*)(sn=%s*)(gn=%s*)(mail=%s*)(zimbraMailDeliveryAddress=%s*)(zimbraMailAlias=%s*))(|(objectclass=zimbraAccount)(objectclass=zimbraDistributionList)))",
         "zimbraSearch:(&(|(displayName=*%s*)(cn=*%s*)(sn=*%s*)(gn=*%s*)(mail=*%s*)(zimbraMailDeliveryAddress=*%s*)(zimbraMailAlias=*%s*))(|(objectclass=zimbraAccount)(objectclass=zimbraDistributionList)))"
        };
         
        Map<String, Object> attr = new HashMap<String, Object>();
        attr.put("+" + Provisioning.A_zimbraGalLdapFilterDef, value);
        
        printer.println("Adding zimbraAutoComplete and zimbraSearch filters to global config " + Provisioning.A_zimbraGalLdapFilterDef);
        prov.modifyAttrs(config, attr);
    }
    

}

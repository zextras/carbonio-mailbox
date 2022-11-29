// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZLdapContext;

public class BUG_57425 extends UpgradeOp {

    @Override
    void doUpgrade() throws ServiceException {
        ZLdapContext zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
        try {
            doGlobalConfig(zlc);
            doAllServers(zlc);
        } finally {
            LdapClient.closeContext(zlc);
        }
    }
    
    private void doEntry(ZLdapContext zlc, Entry entry, String entryName) throws ServiceException {
        /* 
         * after bug 58514, SMIMECertificate is deprecated and no longer in ContactConstants
         * 
         * Define it here.
         */
        final String SMIMECertificate = "SMIMECertificate";
        
        String attrName = Provisioning.A_zimbraContactHiddenAttributes;
        
        printer.println();
        printer.println("Checking " + entryName);
        
        String curValue = entry.getAttr(attrName, false);
        
        boolean needsUpdate;
        
        if (curValue == null) {
            if (entry instanceof Config) {
                needsUpdate = true;
            } else {
                return;
            }
        } else {
            needsUpdate = !curValue.contains(SMIMECertificate);
        }
        
        if (needsUpdate) {
            String newValue;
            
            if (curValue == null) {
                newValue = SMIMECertificate;
            } else {
                newValue = curValue + "," + SMIMECertificate;
            }
            
            Map<String, Object> attrs = new HashMap<String, Object>();
            StringUtil.addToMultiMap(attrs, attrName, newValue);
            modifyAttrs(entry, attrs);
        } else {
            printer.println("    " + attrName + " already has an effective value: [" + curValue + "] on entry " + entryName + " - skipping"); 
        }
    }
    
    private void doGlobalConfig(ZLdapContext zlc) throws ServiceException {
        Config config = prov.getConfig();
        doEntry(zlc, config, "global config");
    }
    
    private void doAllServers(ZLdapContext zlc) throws ServiceException {
        List<Server> servers = prov.getAllServers();
        
        for (Server server : servers) {
            doEntry(zlc, server, "server " + server.getName());
        }
    }


}

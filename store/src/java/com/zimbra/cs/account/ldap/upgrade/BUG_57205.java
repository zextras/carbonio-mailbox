// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Strings;
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

public class BUG_57205 extends UpgradeOp {

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
    
    void doEntry(ZLdapContext zlc, Entry entry) throws ServiceException {
        printer.printCheckingEntry(entry);
        
        String attrName = Provisioning.A_zimbraReverseProxyImapEnabledCapability;
        
        String[] curValues = entry.getMultiAttr(attrName, false);
        if (curValues.length == 0) {
            // no value on entry, do not update
            return;
        }
        
        List<String> curValueList = Arrays.asList(curValues);
                
        Map<String, Object> attrs = new HashMap<String, Object>();
        
        if (!curValueList.contains("LIST-STATUS")) {
            StringUtil.addToMultiMap(attrs, "+" + attrName, "LIST-STATUS");
        }
        
        if (!curValueList.contains("XLIST")) {
            StringUtil.addToMultiMap(attrs, "+" + attrName, "XLIST");
        }
        
        if (!attrs.isEmpty()) {
            modifyAttrs(entry, attrs);
        }
    }
    
    
    private void doGlobalConfig(ZLdapContext zlc) throws ServiceException {
        Config config = prov.getConfig();
        doEntry(zlc, config);
    }
    
    private void doAllServers(ZLdapContext zlc) throws ServiceException {
        List<Server> servers = prov.getAllServers();
        
        for (Server server : servers) {
            doEntry(zlc, server);
        }
    }

}
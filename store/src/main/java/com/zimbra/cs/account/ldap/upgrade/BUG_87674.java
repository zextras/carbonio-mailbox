// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Entry.EntryType;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZLdapContext;

public class BUG_87674 extends UpgradeOp {

    private static final String ATTR_NAME = Provisioning.A_zimbraPrefSkin;
    private static final String FROM_VALUE = "harmony";
    private static final String TO_VALUE = "zextras";

    @Override
    void doUpgrade() throws ServiceException {
        ZLdapContext zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
        try {
            doDomains(zlc);
            doCos(zlc);
        } finally {
            LdapClient.closeContext(zlc);
        }
    }

    @Override
    Description getDescription() {
        return new Description(this,
                new String[] {ATTR_NAME},
                new EntryType[] {EntryType.DOMAIN, EntryType.COS},
                FROM_VALUE,
                TO_VALUE,
                "Update skin to 8.5 default for all COS/Domains where it is set to the previous default");
    }

    private void doDomains(ZLdapContext zlc) throws ServiceException {
        List<Domain> domains = prov.getAllDomains();
        if (domains != null) {
            for (Domain domain : domains) {
                String skin = domain.getAttr(ATTR_NAME, "");
                if (FROM_VALUE.equalsIgnoreCase(skin)) {
                    Map<String, Object> attrs = new HashMap<String, Object>();
                    attrs.put(ATTR_NAME, TO_VALUE);
                    modifyAttrs(domain, attrs);
                }
            }
        }
    }

    private void doCos(ZLdapContext zlc) throws ServiceException {
        List<Cos> classes = prov.getAllCos();
        if (classes != null) {
            for (Cos cos : classes) {
                String skin = cos.getAttr(ATTR_NAME, "");
                if (FROM_VALUE.equalsIgnoreCase(skin)) {
                    Map<String, Object> attrs = new HashMap<String, Object>();
                    attrs.put(ATTR_NAME, TO_VALUE);
                    modifyAttrs(cos, attrs);
                }
            }
        }
    }
}
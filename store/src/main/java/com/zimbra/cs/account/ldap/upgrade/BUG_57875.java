// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Entry.EntryType;
import com.zimbra.cs.ldap.IAttributes;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.SearchLdapOptions;
import com.zimbra.cs.ldap.ZAttributes;
import com.zimbra.cs.ldap.ZLdapContext;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.ldap.ZMutableEntry;

public class BUG_57875 extends UpgradeOp {
    
    private static final String ATTR_NAME = Provisioning.A_zimbraGalLdapGroupHandlerClass;
    private static final String OLD_VALUE = "com.zimbra.cs.gal.ADGalGroupHandler";
    private static final String NEW_VALUE = "com.zimbra.cs.account.grouphandler.ADGroupHandler";
    
    @Override
    void doUpgrade() throws ServiceException {
        ZLdapContext zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
        try {
            upgradeGlobalConfig(zlc);
            upgradeDomains(zlc);
            upgradeDataSources(zlc);
        } finally {
            LdapClient.closeContext(zlc);
        }
    }
    
    @Override
    Description getDescription() {
        return new Description(this, 
                new String[] {ATTR_NAME}, 
                new EntryType[] {EntryType.GLOBALCONFIG, EntryType.DOMAIN, EntryType.DATASOURCE},
                OLD_VALUE, 
                NEW_VALUE, 
                null);
    }
    
    private void upgradeGlobalConfig(ZLdapContext zlc) throws ServiceException {
        String bases[] = new String[] {prov.getDIT().configBranchBaseDN()};
        
        String query = "(&" + ZLdapFilterFactory.getInstance().globalConfig().toFilterString() + 
        "(" + ATTR_NAME + "=" + OLD_VALUE + ")"+ ")";
    
        upgrade(zlc, bases, query);
    }
    
    private void upgradeDomains(ZLdapContext zlc) {
        String bases[] = prov.getDIT().getSearchBases(Provisioning.SD_DOMAIN_FLAG);
        String query = "(&" + ZLdapFilterFactory.getInstance().allDomains().toFilterString() + 
            "(" + ATTR_NAME + "=" + OLD_VALUE + ")"+ ")";
        
        upgrade(zlc, bases, query);
    }
    
    private void upgradeDataSources(ZLdapContext zlc) {
        String bases[] = new String[] {prov.getDIT().mailBranchBaseDN()};
        
        String query = "(&" + ZLdapFilterFactory.getInstance().allDataSources().toFilterString() + 
        "(" + ATTR_NAME + "=" + OLD_VALUE + ")"+ ")";
    
        upgrade(zlc, bases, query);
    }
   
    private void upgrade(ZLdapContext modZlc, String bases[], String filter) {
        SearchLdapOptions.SearchLdapVisitor visitor = new Bug57875Visitor(this, modZlc);

        String attrs[] = new String[] {ATTR_NAME};
        
        for (String base : bases) {
            try {
                printer.format("\n=== Searching LDAP: base = %s, filter = %s\n", base, filter);
                prov.searchLdapOnMaster(base, filter, attrs, visitor);
            } catch (ServiceException e) {
                // log and continue
                printer.println("Caught ServiceException while searching " + filter + " under base " + base);
                printer.printStackTrace(e);
            }
        }
    }
    
    private static class Bug57875Visitor extends SearchLdapOptions.SearchLdapVisitor {
        private UpgradeOp upgradeOp;
        private ZLdapContext modZlc;
        
        Bug57875Visitor(UpgradeOp upgradeOp, ZLdapContext modZlc) {
            super(false);
            this.upgradeOp = upgradeOp;
            this.modZlc = modZlc;
        }
        
        @Override
        public void visit(String dn, IAttributes ldapAttrs) {
            try {
                if (upgradeOp.verbose) {
                    upgradeOp.printer.println("Found entry " + dn);
                }
                
                doVisit(dn, (ZAttributes) ldapAttrs);
            } catch (ServiceException e) {
                upgradeOp.printer.println("entry skipped, encountered error while processing entry at:" + dn);
                upgradeOp.printer.printStackTrace(e);
            }
        }
        
        public void doVisit(String dn, ZAttributes ldapAttrs) throws ServiceException {
            String curValue = ldapAttrs.getAttrString(ATTR_NAME);
            if (OLD_VALUE.equals(curValue)) {
                ZMutableEntry entry = LdapClient.createMutableEntry();
                entry.setAttr(ATTR_NAME, NEW_VALUE);
                upgradeOp.replaceAttrs(modZlc, dn, entry);
            }
        }
    }

}

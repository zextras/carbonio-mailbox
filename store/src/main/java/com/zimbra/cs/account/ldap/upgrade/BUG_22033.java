// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapDIT;
import com.zimbra.cs.ldap.IAttributes;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.SearchLdapOptions;
import com.zimbra.cs.ldap.ZAttributes;
import com.zimbra.cs.ldap.ZLdapContext;
import com.zimbra.cs.ldap.ZMutableEntry;
import com.zimbra.cs.ldap.ZSearchScope;

public class BUG_22033 extends UpgradeOp {

    enum Type {
        account,
        alias,
        calendarresource,
        config,
        cos,
        datasource,
        distributionlist,
        domain,
        identity,
        server,
        signature,
        xmppcomponent,
        zimlet;
        
        public static Type fromString(String s) throws ServiceException {
            try {
                return Type.valueOf(s);
            } catch (IllegalArgumentException e) {
                throw ServiceException.INVALID_REQUEST("unknown type: " + s, e);
            }
        }
    }
    
    private Type mType;
    
    @Override
    boolean parseCommandLine(CommandLine cl) {
        String[] args = cl.getArgs();
        if (args.length == 1) {
            try {
                mType = Type.fromString(args[0]);
            } catch (ServiceException e) {
                LdapUpgrade.usage(null, this, "invalid type: " + args[0]);
                return false;
            }
        } else if (args.length != 0) {
            LdapUpgrade.usage(null, this, "invalid arg");
            return false;
        }
        return true;
    }
    
    @Override
    void usage(HelpFormatter helpFormatter) {
        printer.println();
        printer.println("args for bug " + bug + ":");
        printer.println("    [type]");
        printer.println();
        printer.println("type can be: (if omitted, it means all objects)");
        for (Type t : Type.values()) {
            printer.println("    " + t.name());
        }
        printer.println();
    }
    
    private static class Bug22033Visitor extends SearchLdapOptions.SearchLdapVisitor {
        
        private UpgradeOp upgradeOp;
        private ZLdapContext modZlc;
        private int numModified = 0;
        
        private Bug22033Visitor(UpgradeOp upgradeOp, ZLdapContext modZlc) {
            super(false);
            this.upgradeOp = upgradeOp;
            this.modZlc = modZlc;
        }
        
        @Override
        public void visit(String dn, IAttributes ldapAttrs) {
            try {
                doVisit(dn, (ZAttributes) ldapAttrs);
            } catch (ServiceException e) {
                upgradeOp.printer.println("entry skipped, encountered error while processing entry at:" + dn);
                upgradeOp.printer.printStackTrace(e);
            }
        }
        
        private void doVisit(String dn, ZAttributes ldapAttrs) throws ServiceException {
            
            String createTime = ldapAttrs.getAttrString("createTimestamp");
            
            ZMutableEntry entry = LdapClient.createMutableEntry();
            entry.setAttr(Provisioning.A_zimbraCreateTimestamp, createTime);
            upgradeOp.replaceAttrs(modZlc, dn, entry);
            
            numModified++;
        }
        
        private int getNumModified() {
            return numModified;
        }
    }
    
    @Override
    void doUpgrade() throws ServiceException {
        LdapDIT dit = prov.getDIT();
        String base;
        String query;
        String returnAttrs[] = new String[] {Provisioning.A_objectClass,
                                             Provisioning.A_zimbraCreateTimestamp,
                                             "createTimestamp"};
        
        if (mType == null) {
            printer.println("Checking all objects\n");
            
            base = dit.zimbraBaseDN();
            query = "(|" +
                     "(objectclass=zimbraAccount)" +
                     "(objectclass=zimbraAlias)" +
                     "(objectclass=zimbraCalendarResource)" +
                     "(objectclass=zimbraGlobalConfig)" +
                     "(objectclass=zimbraCOS)" +
                     "(objectclass=zimbraDataSource)" +
                     "(objectclass=zimbraDistributionList)" +
                     "(objectclass=zimbraDomain)" +
                     "(objectclass=zimbraIdentity)" +
                     "(objectclass=zimbraServer)" +
                     "(objectclass=zimbraSignature)" +
                     "(objectclass=zimbraXMPPComponent)" +
                     "(objectclass=zimbraZimletEntry)" +
                     ")";
        } else {
            printer.println("Checking " + mType.name() + " objects...\n");
            
            switch (mType) {
            case account:
                base = dit.mailBranchBaseDN();
                query = "(&(objectclass=zimbraAccount)(!(objectclass=zimbraCalendarResource)))";
                break;
            case alias:
                base = dit.mailBranchBaseDN();
                query = "(objectclass=zimbraAlias)";
                break;
            case calendarresource:
                base = dit.mailBranchBaseDN();
                query = "(objectclass=zimbraCalendarResource)";
                break;
            case config:
                base = dit.configDN();
                query = "(objectclass=zimbraGlobalConfig)";
                break;
            case cos:
                base = dit.cosBaseDN();
                query = "(objectclass=zimbraCOS)";
                break;
            case datasource:
                base = dit.mailBranchBaseDN();
                query = "(objectclass=zimbraDataSource)";
                break;
            case distributionlist:
                base = dit.mailBranchBaseDN();
                query = "(objectclass=zimbraDistributionList)";
                break;
            case domain:
                base = dit.domainBaseDN();
                query = "(objectclass=zimbraDomain)";
                break;
            case identity:
                base = dit.mailBranchBaseDN();
                query = "(objectclass=zimbraIdentity)";
                break;
            case server:
                base = dit.serverBaseDN();
                query = "(objectclass=zimbraServer)";
                break;
            case signature:
                base = dit.mailBranchBaseDN();
                query = "(objectclass=zimbraSignature)";
                break;
            case xmppcomponent:
                base = dit.xmppcomponentBaseDN();
                query = "(objectclass=zimbraXMPPComponent)";
                break;
            case zimlet:
                base = dit.zimletBaseDN();
                query = "(objectclass=zimbraZimletEntry)";
                break;
            default:
                throw ServiceException.FAILURE("", null);    
            }
        }
        
        query = "(&" + "(!(zimbraCreateTimestamp=*))" + query + ")";
        
        ZLdapContext zlc = null; 
        ZLdapContext modZlc = null;
        Bug22033Visitor visitor = null;
        
        try {
            zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
            modZlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UPGRADE);
            
            visitor = new Bug22033Visitor(this, modZlc);
            
            SearchLdapOptions searchOpts = new SearchLdapOptions(base, getFilter(query), 
                    returnAttrs, SearchLdapOptions.SIZE_UNLIMITED, null, 
                    ZSearchScope.SEARCH_SCOPE_SUBTREE, visitor);

            zlc.searchPaged(searchOpts);

        } catch (ServiceException e) {
            throw ServiceException.FAILURE("unable to list all objects", e);
        } finally {
            LdapClient.closeContext(zlc);
            LdapClient.closeContext(modZlc);
            
            if (visitor != null) {
                printer.println("\nModified " + visitor.getNumModified() + " objects");
            }
        }
    }

}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.ldap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.*;

import static org.junit.Assert.*;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapDIT;
import com.zimbra.cs.account.ldap.LdapHelper;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.account.ldap.entry.LdapDomain;
import com.zimbra.cs.account.ldap.entry.LdapEntry;
import com.zimbra.cs.ldap.LdapException.LdapEntryNotFoundException;
import com.zimbra.cs.ldap.LdapException.LdapMultipleEntriesMatchedException;
import com.zimbra.cs.ldap.LdapException.LdapSizeLimitExceededException;
import com.zimbra.cs.ldap.ZLdapFilterFactory.FilterId;
import com.zimbra.cs.ldap.LdapClient;
import com.zimbra.cs.ldap.LdapConstants;
import com.zimbra.cs.ldap.LdapServerType;
import com.zimbra.cs.ldap.LdapUsage;
import com.zimbra.cs.ldap.ZAttributes;
import com.zimbra.cs.ldap.ZLdapContext;
import com.zimbra.cs.ldap.ZLdapFilter;
import com.zimbra.cs.ldap.ZLdapFilterFactory;
import com.zimbra.cs.ldap.ZSearchControls;
import com.zimbra.cs.ldap.ZSearchResultEntry;
import com.zimbra.cs.ldap.ZSearchResultEnumeration;
import com.zimbra.cs.ldap.ZSearchScope;

public class TestLdapHelper extends LdapTest {
    private static LdapProvTestUtil provUtil;
    private static LdapProv prov;
    private static LdapHelper ldapHelper;
    private static ZLdapFilterFactory filterFactory;
    private static Domain domain;
    
    @BeforeClass
    public static void init() throws Exception {
        provUtil = new LdapProvTestUtil();
        prov = provUtil.getProv();
        ldapHelper = provUtil.getProv().getHelper();
        filterFactory = ZLdapFilterFactory.getInstance();
        domain = provUtil.createDomain(baseDomainName(), null);
    }
    
    @AfterClass
    public static void cleanup() throws Exception {
        Cleanup.deleteAll(baseDomainName());
    }
    
    @Test
    public void searchForEntry() throws Exception {
        LdapDIT dit = prov.getDIT();
        String base = dit.configBranchBaseDN();
        ZLdapFilter filter = filterFactory.fromFilterString(FilterId.UNITTEST, "(cn=config)");
        
        ZSearchResultEntry sr = ldapHelper.searchForEntry(
                base, filter, null, false);
        assertNotNull(sr);
        assertEquals("cn=config,cn=zimbra", sr.getDN());
    }
    
    @Test
    public void searchForEntryMultipleMatchedEntries() throws Exception {
        LdapDIT dit = prov.getDIT();
        String base = dit.configBranchBaseDN();
        ZLdapFilter filter = filterFactory.allAccounts();
        
        boolean caughtException = false;
        try {
            ZSearchResultEntry entry = ldapHelper.searchForEntry(
                    base, filter, null, false);
            assertNotNull(entry);
        } catch (LdapMultipleEntriesMatchedException e) {
            caughtException = true;
        }
        assertTrue(caughtException);
    }
    
    @Test
    public void searchForEntryNotFound() throws Exception {
        LdapDIT dit = prov.getDIT();
        String base = dit.configBranchBaseDN();
        ZLdapFilter filter = 
            filterFactory.fromFilterString(FilterId.UNITTEST, "(cn=bogus)");
        
        ZSearchResultEntry sr = ldapHelper.searchForEntry(
                base, filter, null, false);
        assertNull(sr);
    }
    
    @Test
    public void getAttributes() throws Exception {
        String dn = prov.getDIT().configDN();
        ZAttributes attrs = ldapHelper.getAttributes(LdapUsage.UNITTEST, dn);
        assertEquals("config", attrs.getAttrString(Provisioning.A_cn));
    }
    
    @Test
    public void getAttributesEntryNotFound() throws Exception {
        String dn = prov.getDIT().configDN() + "-not";
        
        boolean caughtException = false;
        try {
            ZAttributes attrs = ldapHelper.getAttributes(LdapUsage.UNITTEST, dn);
            
        } catch (LdapEntryNotFoundException e) {
            caughtException = true;
        }
        assertTrue(caughtException);
    }
    
    @Test
    public void searchDir() throws Exception {
        LdapDIT dit = prov.getDIT();
        String base = dit.configBranchBaseDN();
        ZLdapFilter filter = filterFactory.anyEntry();
        String returnAttrs[] = new String[]{"objectClass"};
        
        ZSearchControls searchControls = ZSearchControls.createSearchControls(
                ZSearchScope.SEARCH_SCOPE_ONELEVEL, 
                ZSearchControls.SIZE_UNLIMITED, returnAttrs);
        
        ZSearchResultEnumeration ne = ldapHelper.searchDir(base, filter, searchControls);
        
        Set<String> expected = new HashSet<String>();
        
        expected.add(dit.adminBaseDN());
        expected.add(dit.appAdminBaseDN());
        expected.add(dit.zimletBaseDN());
        expected.add(dit.cosBaseDN());
        expected.add(dit.globalDynamicGroupBaseDN());
        expected.add(dit.serverBaseDN());
        expected.add(dit.xmppcomponentBaseDN());
        expected.add(dit.globalGrantDN());
        expected.add(dit.configDN());
        expected.add(dit.shareLocatorBaseDN());
        expected.add(dit.ucServiceBaseDN());
        
        int numFound = 0;
        while (ne.hasMore()) {
            ZSearchResultEntry sr = ne.next();
            assertTrue(expected.contains(sr.getDN()));
            numFound++;
        }
        ne.close();
        
        assertEquals(expected.size(), numFound);
    }
    
    @Test
    public void searchDirNotFound() throws Exception {
        LdapDIT dit = prov.getDIT();
        String base = dit.configBranchBaseDN();
        ZLdapFilter filter = filterFactory.allSignatures();
        String returnAttrs[] = new String[]{"objectClass"};
        
        ZSearchControls searchControls = ZSearchControls.createSearchControls(
                ZSearchScope.SEARCH_SCOPE_SUBTREE, 
                ZSearchControls.SIZE_UNLIMITED, returnAttrs);
        
        ZSearchResultEnumeration ne = 
            ldapHelper.searchDir(base, filter, searchControls);
        
        int numFound = 0;
        while (ne.hasMore()) {
            ZSearchResultEntry sr = ne.next();
            numFound++;
        }
        ne.close();
        
        assertEquals(0, numFound);
    }
    
    @Test
    public void searchDirSizeLimitExceeded() throws Exception {
        int SIZE_LIMIT = 5;
        
        String base = LdapConstants.DN_ROOT_DSE;
        ZLdapFilter filter = filterFactory.anyEntry();
        String returnAttrs[] = new String[]{"objectClass"};
        
        ZSearchControls searchControls = ZSearchControls.createSearchControls(
                ZSearchScope.SEARCH_SCOPE_SUBTREE, 
                SIZE_LIMIT, returnAttrs);
        
        int numFound = 0;
        boolean caughtException = false;
        try {
            ZSearchResultEnumeration ne = ldapHelper.searchDir(base, filter, searchControls);
            while (ne.hasMore()) {
                ZSearchResultEntry sr = ne.next();
                numFound++;
            }
            ne.close();
            
        } catch (LdapSizeLimitExceededException e) {
            caughtException = true;
        }
        assertTrue(caughtException);
     
        /*
        // unboundid does not return entries if LdapSizeLimitExceededException
        // is thrown,  See commons on ZLdapContext.searchDir().
        if (testConfig != TestLdap.TestConfig.UBID) {
            assertEquals(SIZE_LIMIT, numFound);
        }
        */
    }
    
    @Test
    public void testAndModifyAttributes() throws Exception {
        ZLdapFilter filter = filterFactory.domainLockedForEagerAutoProvision();
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(Provisioning.A_zimbraAutoProvLock, "blah");
        
        ZLdapContext zlc = null;
        try {
            zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UNITTEST);
            ldapHelper.testAndModifyEntry(zlc, ((LdapEntry)domain).getDN(), 
                    filter, attrs, domain);
        } finally {
            LdapClient.closeContext(zlc);
        }
    }
    
    @Test
    public void hasSubordinates() throws Exception {
        Domain domain = provUtil.createDomain(genDomainName(baseDomainName()));
        Account acct = provUtil.createAccount(genAcctNameLocalPart(), domain);
        Group group = provUtil.createGroup(genGroupNameLocalPart(), domain, true);
        
        String domainDn = ((LdapDomain)domain).getDN();
        String acctBaseDn = prov.getDIT().domainDNToAccountBaseDN(domainDn);
        String dynGroupsBaseDn = prov.getDIT().domainDNToDynamicGroupsBaseDN(domainDn);
        
        ZLdapContext zlc = null;
        try {
            zlc = LdapClient.getContext(LdapServerType.MASTER, LdapUsage.UNITTEST);
            
            // verify dn has Subordinates
            assertTrue(hasSubordinates(zlc, acctBaseDn));
            assertTrue(hasSubordinates(zlc, dynGroupsBaseDn));
            
            provUtil.deleteAccount(acct);
            provUtil.deleteGroup(group);
            
            // verify dn don't have Subordinates
            assertFalse(hasSubordinates(zlc, acctBaseDn));
            assertFalse(hasSubordinates(zlc, dynGroupsBaseDn));
            
        } finally {
            LdapClient.closeContext(zlc);
        }
    }
    
    private boolean hasSubordinates(ZLdapContext zlc, String dn) throws Exception {
        boolean hasSubordinates = false;
        
        ZSearchResultEnumeration ne = null;
        try {
            ne = ldapHelper.searchDir(dn, filterFactory.hasSubordinates(), 
                    ZSearchControls.SEARCH_CTLS_SUBTREE(), zlc, LdapServerType.MASTER);
            hasSubordinates = ne.hasMore();
            
            if (hasSubordinates) {
                int numEntries = 0;
                String entryDn = null;
                while (ne.hasMore()) {
                    ZSearchResultEntry sr = ne.next();
                    entryDn = sr.getDN();
                    numEntries++;
                }
                assertEquals(1, numEntries);
                assertEquals(dn, entryDn);
            }
        } finally {
            if (ne != null) {
                ne.close();
            }
        }
        
        return hasSubordinates;
    }
}

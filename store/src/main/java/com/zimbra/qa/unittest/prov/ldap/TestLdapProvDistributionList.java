// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.ldap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.*;

import static org.junit.Assert.*;

import com.google.common.collect.Maps;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.account.Key.DistributionListBy;
import com.zimbra.common.util.StringUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.DistributionList;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.qa.QA.Bug;
import com.zimbra.qa.unittest.TestUtil;
import com.zimbra.qa.unittest.prov.Names;
import com.zimbra.qa.unittest.prov.Verify;
import com.zimbra.soap.admin.type.CacheEntryType;

public class TestLdapProvDistributionList extends LdapTest {
    private static LdapProvTestUtil provUtil;
    private static Provisioning prov;
    private static Domain domain;
    private static String BASE_DOMAIN_NAME;
    
    @BeforeClass
    public static void init() throws Exception {
        provUtil = new LdapProvTestUtil();
        prov = provUtil.getProv();
        domain = provUtil.createDomain(baseDomainName(), null);
        BASE_DOMAIN_NAME = domain.getName();
    }
    
    @AfterClass
    public static void cleanup() throws Exception {
        Cleanup.deleteAll(baseDomainName());
    }
   
    private DistributionList createDistributionList(String localpart) throws Exception {
        return createDistributionList(localpart, null);
    }
    
    private DistributionList createDistributionList(String localPart, Map<String, Object> attrs) 
    throws Exception {
        return provUtil.createDistributionList(localPart, domain, attrs);
    }
    
    private void deleteDistributionList(DistributionList dl) throws Exception {
        provUtil.deleteDistributionList(dl);
    }
    
    private Account createAccount(String localPart) throws Exception {
        return createAccount(localPart, null);
    }
    
    private Account createAccount(String localPart, Map<String, Object> attrs) 
    throws Exception {
        return provUtil.createAccount(localPart, domain, attrs);
    }
    
    private void deleteAccount(Account acct) throws Exception {
        provUtil.deleteAccount(acct);
    }
    
    private void verifyEquals(Set<String> expectedNames, List<DistributionList> actual) 
    throws Exception {
        assertEquals(expectedNames.size(), actual.size());
        Set<String> actualIds = new HashSet<String>();
        for (DistributionList group : actual) {
            actualIds.add(group.getName());
        }
        Verify.verifyEquals(expectedNames, actualIds);
    }
    
    private void verifyMembership(String acctId, 
            Set<String> expectedAllGroups, Set<String> expectedDirectGroups) 
    throws Exception {
        
        // test twice, so we hit all paths in LdapProvisioning.getGroups
        
        for (int i = 0; i < 2; i++) {
            Account acct = prov.get(AccountBy.id, acctId);
            
            List<DistributionList> allGroups = prov.getDistributionLists(acct, false, null);
            verifyEquals(expectedAllGroups, allGroups);
            
            List<DistributionList> directGroups = prov.getDistributionLists(acct, true, null);
            verifyEquals(expectedDirectGroups, directGroups);
        }
    }
    
    private void getDistributionListById(String id) throws Exception {
        prov.flushCache(CacheEntryType.group, null);
        DistributionList dl = prov.get(Key.DistributionListBy.id, id);
        assertNotNull(dl);
        assertEquals(id, dl.getId());
    }
    
    private void getDistributionListByName(String name) throws Exception {
        prov.flushCache(CacheEntryType.group, null);
        DistributionList dl = prov.get(Key.DistributionListBy.name, name);
        assertNotNull(dl);
        assertEquals(name, dl.getName());
    }
    
    @Test
    public void createDistributionList() throws Exception {
        String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart());
        
        DistributionList dl = createDistributionList(DL_NAME_LOCALPART);
        
        // make sure the group has a home server
        Server homeServer = dl.getServer();
        assertNotNull(homeServer);
        
        deleteDistributionList(dl);
    }
    
    @Test
    public void createDistributionListAlreadyExists() throws Exception {
        String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart());
        DistributionList dl = createDistributionList(DL_NAME_LOCALPART);
        
        boolean caughtException = false;
        try {
            prov.createDistributionList(dl.getName(), new HashMap<String, Object>());
        } catch (AccountServiceException e) {
            if (AccountServiceException.DISTRIBUTION_LIST_EXISTS.equals(e.getCode())) {
                caughtException = true;
            }
        }
        assertTrue(caughtException);
        
        deleteDistributionList(dl);
    }
    
    @Test
    public void getDistributionList() throws Exception {
        String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart());
        DistributionList dl = createDistributionList(DL_NAME_LOCALPART);
        String DL_ID = dl.getId();
        
        getDistributionListById(dl.getId());
        getDistributionListByName(dl.getName());
        
        deleteDistributionList(dl);
    }
    
    @Test
    public void renameDistributionList() throws Exception {
        String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart());
        String DL_NEW_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart("new"));
        String DL_NEW_NAME = TestUtil.getAddress(DL_NEW_NAME_LOCALPART, domain.getName()).toLowerCase();
        
        DistributionList dl = createDistributionList(DL_NAME_LOCALPART);
        String DL_ID = dl.getId();
        
        // set zimbraPrefAllowAddressForDelegatedSender
        Map<String, Object> attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraPrefAllowAddressForDelegatedSender, dl.getName());
        prov.modifyAttrs(dl, attrs);
        
        prov.renameDistributionList(DL_ID, DL_NEW_NAME);
        
        prov.flushCache(CacheEntryType.group, null);
        getDistributionListById(DL_ID);
        getDistributionListByName(DL_NEW_NAME);
        
        // make sure zimbraPrefAllowAddressForDelegatedSender is updated
        DistributionList renamedDl = prov.get(DistributionListBy.name, DL_NEW_NAME);
        Set<String> addrsForDelegatedSender = renamedDl.getMultiAttrSet(Provisioning.A_zimbraPrefAllowAddressForDelegatedSender);
        assertEquals(1, addrsForDelegatedSender.size());
        assertTrue(addrsForDelegatedSender.contains(DL_NEW_NAME));
        
        deleteDistributionList(dl);
    }
    
    @Test
    public void renameDistributionListDomainChanged() throws Exception {
        String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart());
        
        String NEW_DOMAIN_NAME = genDomainSegmentName() + "." + BASE_DOMAIN_NAME;
        Domain newDomain = provUtil.createDomain(NEW_DOMAIN_NAME, null);
        String DL_NEW_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart("new"));
        String DL_NEW_NAME = TestUtil.getAddress(DL_NEW_NAME_LOCALPART, newDomain.getName()).toLowerCase();
        
        DistributionList dl = createDistributionList(DL_NAME_LOCALPART);
        String DL_ID = dl.getId();
        
        // set zimbraPrefAllowAddressForDelegatedSender
        Map<String, Object> attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraPrefAllowAddressForDelegatedSender, dl.getName());
        prov.modifyAttrs(dl, attrs);
        
        prov.renameDistributionList(DL_ID, DL_NEW_NAME);
        
        getDistributionListById(DL_ID);
        getDistributionListByName(DL_NEW_NAME);
        
        // make sure zimbraPrefAllowAddressForDelegatedSender is updated
        DistributionList renamedDl = prov.get(DistributionListBy.name, DL_NEW_NAME);
        Set<String> addrsForDelegatedSender = renamedDl.getMultiAttrSet(Provisioning.A_zimbraPrefAllowAddressForDelegatedSender);
        assertEquals(1, addrsForDelegatedSender.size());
        assertTrue(addrsForDelegatedSender.contains(DL_NEW_NAME));
        
        deleteDistributionList(dl);
        provUtil.deleteDomain(newDomain);
    }
    
    @Test
    public void addMembers() throws Exception {
        String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart());
        String MEMBER_1 = "member_1@test.com";
        String MEMBER_2 = "member_2@test.com";
        String MEMBER_3 = "member_3@test.com";
        String MEMBER_4 = "member_4@test.com";
        
        Map<String, Object> attrs = new HashMap<String, Object>();
        // add initial members
        StringUtil.addToMultiMap(attrs, Provisioning.A_zimbraMailForwardingAddress, MEMBER_1);
        StringUtil.addToMultiMap(attrs, Provisioning.A_zimbraMailForwardingAddress, MEMBER_2);
        
        DistributionList dl = createDistributionList(DL_NAME_LOCALPART, attrs);
        
        prov.addMembers(dl, new String[]{MEMBER_3, MEMBER_4});
        
        Set<String> allMembers = dl.getAllMembersSet();
        assertEquals(4, allMembers.size());
        assertTrue(allMembers.contains(MEMBER_1));
        assertTrue(allMembers.contains(MEMBER_2));
        assertTrue(allMembers.contains(MEMBER_3));
        assertTrue(allMembers.contains(MEMBER_4));
        
        deleteDistributionList(dl);
    }
    
    @Test
    public void removeMembers() throws Exception {
        String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart());
        String MEMBER_1 = "member_1@test.com";
        String MEMBER_2 = "member_2@test.com";
        String MEMBER_3 = "member_3@test.com";
        String MEMBER_4 = "member_4@test.com";
        
        Map<String, Object> attrs = new HashMap<String, Object>();
        // add initial members
        StringUtil.addToMultiMap(attrs, Provisioning.A_zimbraMailForwardingAddress, MEMBER_1);
        StringUtil.addToMultiMap(attrs, Provisioning.A_zimbraMailForwardingAddress, MEMBER_2);
        StringUtil.addToMultiMap(attrs, Provisioning.A_zimbraMailForwardingAddress, MEMBER_3);
        StringUtil.addToMultiMap(attrs, Provisioning.A_zimbraMailForwardingAddress, MEMBER_4);
        
        DistributionList dl = createDistributionList(DL_NAME_LOCALPART, attrs);
        
        prov.removeMembers(dl, new String[]{MEMBER_3, MEMBER_4});
        
        Set<String> allMembers = dl.getAllMembersSet();
        assertEquals(2, allMembers.size());
        assertTrue(allMembers.contains(MEMBER_1));
        assertTrue(allMembers.contains(MEMBER_2));
        
        deleteDistributionList(dl);
    }
    
    @Test
    public void accountInDistributionList() throws Exception {
        String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart());
        DistributionList dl = createDistributionList(DL_NAME_LOCALPART);
        String DL_ID = dl.getId();
        
        String ACCT_NAME_IN_DL_LOCALPART = Names.makeAccountNameLocalPart(genAcctNameLocalPart("acct-in"));
        Account acctInDL = createAccount(ACCT_NAME_IN_DL_LOCALPART);
        String ACCT_NAME_NOT_IN_DL_LOCALPART = Names.makeAccountNameLocalPart(genAcctNameLocalPart("acct-not-in"));
        Account acctNotInDL = createAccount(ACCT_NAME_NOT_IN_DL_LOCALPART);
        
        prov.addMembers(dl, new String[]{acctInDL.getName()});
        
        boolean inDL = prov.inDistributionList(acctInDL, DL_ID);
        assertTrue(inDL);
        
        inDL = prov.inDistributionList(acctNotInDL, DL_ID);
        assertFalse(inDL);
        
        deleteAccount(acctInDL);
        deleteAccount(acctNotInDL);
        deleteDistributionList(dl);
    }
    
    @Test
    public void dlInDistributionList() throws Exception {
        String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart());
        DistributionList dl = createDistributionList(DL_NAME_LOCALPART);
        String DL_ID = dl.getId();
        
        String DL_NAME_IN_DL_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart("dl-in"));
        DistributionList dlInDL = createDistributionList(DL_NAME_IN_DL_LOCALPART);
        String DL_NAME_NOT_IN_DL_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart("dl-not-in"));
        DistributionList dlNotInDL = createDistributionList(DL_NAME_NOT_IN_DL_LOCALPART);
        
        prov.addMembers(dl, new String[]{dlInDL.getName()});
        
        boolean inDL = prov.inDistributionList(dlInDL, DL_ID);
        assertTrue(inDL);
        
        inDL = prov.inDistributionList(dlNotInDL, DL_ID);
        assertFalse(inDL);
        
        deleteDistributionList(dlInDL);
        deleteDistributionList(dlNotInDL);
        deleteDistributionList(dl);
    }
    
    @Test
    public void getAccountDistributionLists() throws Exception {
        String DL_NAME_1_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart("dl-1"));
        String DL_NAME_2_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart("dl-2"));
        DistributionList dl1 = createDistributionList(DL_NAME_1_LOCALPART);
        DistributionList dl2 = createDistributionList(DL_NAME_2_LOCALPART);
        
        String ACCT_NAME_LOCALPART = Names.makeAccountNameLocalPart(genAcctNameLocalPart("acct"));
        Account acct = createAccount(ACCT_NAME_LOCALPART);
        
        prov.addMembers(dl1, new String[]{dl2.getName()});
        prov.addMembers(dl2, new String[]{acct.getName()});
        
        Set<String> dlIds = prov.getDistributionLists(acct);
        assertEquals(2, dlIds.size());
        assertTrue(dlIds.contains(dl1.getId()));
        assertTrue(dlIds.contains(dl2.getId()));
        
        Map<String, String> via = new HashMap<String,String>();
        List<DistributionList> inDlsDirect = prov.getDistributionLists(acct, true, via);
        assertEquals(1, inDlsDirect.size());
        assertEquals(dl2.getId(), inDlsDirect.get(0).getId());
        
        List<DistributionList> inDlsAll = prov.getDistributionLists(acct, false, via);
        assertEquals(2, inDlsAll.size());
        
        deleteAccount(acct);
        deleteDistributionList(dl1);
        deleteDistributionList(dl2);
    }
    
    @Test
    public void getDlDistributionLists() throws Exception {
        String DL_NAME_1_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart("dl-1"));
        String DL_NAME_2_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart("dl-2"));
        DistributionList dl1 = createDistributionList(DL_NAME_1_LOCALPART);
        DistributionList dl2 = createDistributionList(DL_NAME_2_LOCALPART);
        
        String DL_NAME_LOCALPART = Names.makeDLNameLocalPart(genGroupNameLocalPart("dl"));
        DistributionList dl = createDistributionList(DL_NAME_LOCALPART);
        
        prov.addMembers(dl1, new String[]{dl2.getName()});
        prov.addMembers(dl2, new String[]{dl.getName()});
        
        Map<String, String> via = new HashMap<String,String>();
        List<DistributionList> inDlsDirect = prov.getDistributionLists(dl, true, via);
        assertEquals(1, inDlsDirect.size());
        assertEquals(dl2.getId(), inDlsDirect.get(0).getId());
        
        List<DistributionList> inDlsAll = prov.getDistributionLists(dl, false, via);
        assertEquals(2, inDlsAll.size());
        
        deleteDistributionList(dl);
        deleteDistributionList(dl1);
        deleteDistributionList(dl2);
    }
    

    @Test
    public void testCircular1() throws Exception {
        Domain domain = provUtil.createDomain(genDomainSegmentName() + "." + BASE_DOMAIN_NAME);
        
        DistributionList group1 = provUtil.createDistributionList("group1", domain);
        DistributionList group2 = provUtil.createDistributionList("group2", domain);
        Account acct1 = provUtil.createAccount("acct1", domain);
        Account acct2 = provUtil.createAccount("acct2", domain);
        String acct1Id = acct1.getId();
        String acct2Id = acct2.getId();
        
        group1.addMembers(new String[]{group2.getName(), acct1.getName(), acct2.getName()});
        group2.addMembers(new String[]{group1.getName(), acct1.getName(), acct2.getName()});
        
        Set<String> expectedAllGroups = new HashSet<String>();
        Set<String> expectedDirectGroups = new HashSet<String>();
        expectedAllGroups.add(group1.getName());
        expectedAllGroups.add(group2.getName());
        expectedDirectGroups.add(group1.getName());
        expectedDirectGroups.add(group2.getName());

        verifyMembership(acct1Id, expectedAllGroups, expectedDirectGroups);
        verifyMembership(acct2Id, expectedAllGroups, expectedDirectGroups);
    }
    
    @Test
    public void testCircular2() throws Exception {
        Domain domain = provUtil.createDomain(genDomainSegmentName() + "." + BASE_DOMAIN_NAME);
        
        DistributionList group1 = provUtil.createDistributionList("group1", domain);
        DistributionList group2 = provUtil.createDistributionList("group2", domain);
        DistributionList group3 = provUtil.createDistributionList("group3", domain);
        Account acct1 = provUtil.createAccount("acct1", domain);
        Account acct2 = provUtil.createAccount("acct2", domain);
        String acct1Id = acct1.getId();
        String acct2Id = acct2.getId();
        
        group1.addMembers(new String[]{group2.getName(), acct1.getName(), acct2.getName()});
        group2.addMembers(new String[]{group3.getName()});
        group3.addMembers(new String[]{group1.getName()});
        
        Set<String> expectedAllGroups = new HashSet<String>();
        Set<String> expectedDirectGroups = new HashSet<String>();
        expectedAllGroups.add(group1.getName());
        expectedAllGroups.add(group2.getName());
        expectedAllGroups.add(group3.getName());
        expectedDirectGroups.add(group1.getName());

        verifyMembership(acct1Id, expectedAllGroups, expectedDirectGroups);
        verifyMembership(acct2Id, expectedAllGroups, expectedDirectGroups);
    }
    

    @Test
    @Bug(bug=42132)
    // @Ignore  // LdapProvisioning no long allows adding self as a group 
    public void bug42132() throws Exception {
        Domain domain = provUtil.createDomain(genDomainSegmentName() + "." + BASE_DOMAIN_NAME);
        
        DistributionList group1 = provUtil.createDistributionList("group1", domain);
        DistributionList group2 = provUtil.createDistributionList("group2", domain);
        Account acct1 = provUtil.createAccount("acct1", domain);
        Account acct2 = provUtil.createAccount("acct2", domain);
        String acct1Id = acct1.getId();
        String acct2Id = acct2.getId();
        
        group2.addMembers(new String[]{group1.getName()});
        
        // group2.addMembers(new String[]{group2.getName()}); // no longer allowed in LdapProvisioning
        Map<String, Object> attrs = Maps.newHashMap();
        attrs.put("+" + Provisioning.A_zimbraMailForwardingAddress, group2.getName());
        prov.modifyAttrs(group2, attrs);// modifyAttrs directly
        
        group1.addMembers(new String[]{acct1.getName(), acct2.getName()});
        
        Set<String> expectedAllGroups = new HashSet<String>();
        Set<String> expectedDirectGroups = new HashSet<String>();
        expectedAllGroups.add(group1.getName());
        expectedAllGroups.add(group2.getName());
        expectedDirectGroups.add(group1.getName());

        verifyMembership(acct1Id, expectedAllGroups, expectedDirectGroups);
        verifyMembership(acct2Id, expectedAllGroups, expectedDirectGroups);
    }
}

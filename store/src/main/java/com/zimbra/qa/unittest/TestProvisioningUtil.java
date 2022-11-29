// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.cs.account.NamedEntry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.account.soap.SoapProvisioning;


public class TestProvisioningUtil extends TestCase {
    
    private static String NAME_ROOT_DOMAIN     = "ldaptest";
    
    public static String genTestId() {
        Date date = new Date();
        SimpleDateFormat fmt =  new SimpleDateFormat("yyyyMMdd-HHmmss");
        return fmt.format(date);
    }
    
    public static String baseDomainName(String testName) {
        return testName + "-" + genTestId() + "." + NAME_ROOT_DOMAIN;
    }
    
    public static String baseDomainName(String testName, String testId) {
        if (testId == null)
            return testName + "." + NAME_ROOT_DOMAIN;
        else
            return testName + "-" + testId + "." + NAME_ROOT_DOMAIN;
    }

    public static void verifySameId(NamedEntry entry1, NamedEntry entry2) throws Exception {
        assertNotNull(entry1);
        assertNotNull(entry2);
        assertEquals(entry1.getId(), entry2.getId());
    }
    
    
    public static void verifySameEntry(NamedEntry entry1, NamedEntry entry2) throws Exception {
        verifySameId(entry1, entry2);
        assertEquals(entry1.getName(), entry2.getName());
    }
    
    // verify list contains all the entries
    // if checkCount == true, verify the count matches too
    public static void verifyEntries(List<NamedEntry> list, NamedEntry[] entries, boolean checkCount) throws Exception {
        try {
            if (checkCount)
                assertEquals(list.size(), entries.length);
        
            Set<String> ids = new HashSet<String>();
            for (NamedEntry entry : list)
                ids.add(entry.getId());
            
            for (NamedEntry entry : entries) {
                assertTrue(ids.contains(entry.getId()));
                ids.remove(entry.getId());
            }
            
            // make sure all ids in list is present is entries
            if (checkCount)
                assertEquals(ids.size(), 0);
         
        } catch (AssertionFailedError e) {
            System.out.println("\n===== verifyEntries failed =====");
            System.out.println("Message:" + e.getMessage());
            
            System.out.println("\nlist contains " + list.size() + " entries:");
            for (NamedEntry entry : list)
                System.out.println("    " + entry.getName());
            System.out.println("entries contains " + entries.length + " entries:");
            for (NamedEntry entry : entries)
                System.out.println("    " + entry.getName());
            
            System.out.println();
            throw e;
        }
    }
    
    // verify list of NamedEntry contains all the ids
    // if checkCount == true, verify the count matches too
    public static void verifyEntriesById(List<NamedEntry> list, String[] names, boolean checkCount) throws Exception {
        Set<String> idsInList = new HashSet<String>();
        for (NamedEntry entry : list)
            idsInList.add(entry.getId());
        
        verifyEntries(idsInList, names, checkCount);
    }
    
    // verify list of NamedEntry contains all the names
    // if checkCount == true, verify the count matches too
    public static void verifyEntriesByName(List<NamedEntry> list, String[] names, boolean checkCount) throws Exception {
        Set<String> namesInList = new HashSet<String>();
        for (NamedEntry entry : list)
            namesInList.add(entry.getName());
        
        verifyEntries(namesInList, names, checkCount);
    }
    
    // verify list contains all the names
    // if checkCount == true, verify the count matches too
    public static void verifyEntries(Set<String> list, String[] names, boolean checkCount) throws Exception {
        try {
            if (checkCount)
                assertEquals(names.length, list.size());
            
            for (String name : names)
                assertTrue(list.contains(name));
         
        } catch (AssertionFailedError e) {
            System.out.println("\n===== verifyEntries failed =====");
            System.out.println("Message:" + e.getMessage());
            
            System.out.println("\nlist contains " + list.size() + " entries:");
            for (String name : list)
                System.out.println("    " + name);
            System.out.println("entries contains " + names.length + " entries:");
            for (String name : names)
                System.out.println("    " + name);
            
            System.out.println();
            throw e;
        }
    }
    
    public static void verifyEquals(Set<String> expected, Set<String> actual) throws Exception {
       
        assertEquals(expected.size(), actual.size());
        
        for (String e : expected)
            assertTrue(actual.contains(e));
        
        for (String a : actual)
            assertTrue(expected.contains(a));
    }
    
    
    public static LdapProv getLdapProvisioning() throws ServiceException {
        LdapProv lp = (LdapProv)Provisioning.getInstance();
        return lp;
    }
    
    public static SoapProvisioning getSoapProvisioning() throws ServiceException {
        SoapProvisioning sp = new SoapProvisioning();
        sp.soapSetTransportTimeout(0); // use infinite timeout
        sp.soapSetURI("https://localhost:7071" + AdminConstants.ADMIN_SERVICE_URI);
        sp.soapZimbraAdminAuthenticate();
        return sp;
    }
    
    public static SoapProvisioning getSoapProvisioning(String userName, String password) throws ServiceException {
        SoapProvisioning sp = new SoapProvisioning();
        sp.soapSetURI("https://localhost:7071" + AdminConstants.ADMIN_SERVICE_URI);
        sp.soapAdminAuthenticate(userName, password);
        return sp;
    }
    
    /**
     * returns a SoapProvisioningUser object, authenticated as a user, not admin
     * @param userName
     * @param password
     * @return
     * @throws ServiceException
     */
    public static SoapProvisioningUser getSoapProvisioningUser(String userName, String password) throws ServiceException {
        SoapProvisioningUser spu = new SoapProvisioningUser(userName, password);
        spu.auth();
        return spu;
    }
    
    public static class SoapProvisioningUser extends SoapProvisioning {
        
        String mName;
        String mPassword;
        
        SoapProvisioningUser(String name, String password) {
            mName = name;
            mPassword = password;
            setURL();
        }
        
        private void setURL() {
            soapSetURI(TestUtil.getSoapUrl());
        }
        
        private void auth() throws ServiceException {
            XMLElement req = new XMLElement(AccountConstants.AUTH_REQUEST);
            Element a = req.addElement(AccountConstants.E_ACCOUNT);
            a.addAttribute(AccountConstants.A_BY, "name");
            a.setText(mName);
            req.addElement(AccountConstants.E_PASSWORD).setText(mPassword);
            Element response = invoke(req);
            String authToken = response.getElement(AccountConstants.E_AUTH_TOKEN).getText();
            setAuthToken(new ZAuthToken(authToken));
        }
 
        /* 
         * invokeOnTargetAccount in SoapProvisioning is protected, 
         * expose it here
         */
        public Element invokeOnTargetAccount(Element request, String targetId) throws ServiceException {
            return super.invokeOnTargetAccount(request, targetId);
        }

    }
}

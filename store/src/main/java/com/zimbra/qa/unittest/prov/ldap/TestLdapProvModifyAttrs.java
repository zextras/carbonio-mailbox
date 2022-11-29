// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.ldap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.*;
import static org.junit.Assert.*;

import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.ldap.unboundid.InMemoryLdapServer;
import com.zimbra.qa.unittest.TestUtil;
import com.zimbra.qa.unittest.prov.BinaryLdapData;
import com.zimbra.soap.admin.type.CacheEntryType;

public class TestLdapProvModifyAttrs extends LdapTest {
    private static LdapProvTestUtil provUtil;
    private static Provisioning prov;
    private static Domain domain;
    
    @BeforeClass
    public static void init() throws Exception {
        provUtil = new LdapProvTestUtil();
        prov = provUtil.getProv();
        domain = provUtil.createDomain(baseDomainName());
    }
    
    @AfterClass
    public static void cleanup() throws Exception {
        Cleanup.deleteAll(baseDomainName());
    }
    
    private Account createAccount(String localPart) throws Exception {
        return createAccount(localPart, null);
    }
    
    private Account createAccount(String localPart, Map<String, Object> attrs) throws Exception {
        String acctName = TestUtil.getAddress(localPart, domain.getName());
        Account acct = prov.get(AccountBy.name, acctName);
        assertNull(acct);
                
        acct = prov.createAccount(acctName, "test123", attrs);
        assertNotNull(acct);
        return acct;
    }
    
    private void deleteAccount(Account acct) throws Exception {
        String acctId = acct.getId();
        prov.deleteAccount(acctId);
        acct = prov.get(AccountBy.id, acctId);
        assertNull(acct);
    }
    
    // read the entry from ldap 
    private Account getFresh(Account acct) throws Exception {
        prov.flushCache(CacheEntryType.account, null);
        return prov.get(AccountBy.id, acct.getId());
    }
    
    private void modifySingleValue(Account acct, String attrName, String attrValue) throws Exception {
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(attrName, attrValue);
        prov.modifyAttrs(acct, attrs);
        acct = getFresh(acct);
        
        // modifying an attr value to "" will unset the attr
        String expectedValue = (attrValue == "") ? null : attrValue;
        assertEquals(expectedValue, acct.getAttr(attrName));
    }
    
    private void modifyMultiValue(Account acct, String attrName, String[] attrValues) throws Exception {
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put(attrName, attrValues);
        prov.modifyAttrs(acct, attrs);
    }
    
    @Test
    public void setSingleValue() throws Exception {
        String ATTR_NAME = Provisioning.A_displayName;
        String ATTR_VALUE = "setSingleValue";
        
        Account acct = createAccount(genAcctNameLocalPart());
        
        // run twice, ensure it is handled correctly
        modifySingleValue(acct, ATTR_NAME, ATTR_VALUE);
        modifySingleValue(acct, ATTR_NAME, ATTR_VALUE);
        
        deleteAccount(acct);
    }
    
    @Test
    public void setSingleValueBinary() throws Exception {
        String ATTR_NAME = Provisioning.A_userSMIMECertificate;
        String ATTR_VALUE = BinaryLdapData.Content.generateContent(32).getString();
        
        Account acct = createAccount(genAcctNameLocalPart());
        
        // run twice, ensure it is handled correctly
        modifySingleValue(acct, ATTR_NAME, ATTR_VALUE);
        modifySingleValue(acct, ATTR_NAME, ATTR_VALUE);
        
        deleteAccount(acct);
    }
    
    @Test
    public void unsetSingleValue() throws Exception {
        String ATTR_NAME = Provisioning.A_displayName;
        String ATTR_VALUE = "unsetSingleValue";
        
        Account acct = createAccount(genAcctNameLocalPart());
        modifySingleValue(acct, ATTR_NAME, ATTR_VALUE);
        
        // run twice, ensure it is handled correctly
        modifySingleValue(acct, ATTR_NAME, null);
        modifySingleValue(acct, ATTR_NAME, null);
        modifySingleValue(acct, ATTR_NAME, "");
        modifySingleValue(acct, ATTR_NAME, "");
        
        deleteAccount(acct);
    }
    
    @Test
    public void addMultiValue() throws Exception {
        String ATTR_NAME = Provisioning.A_zimbraACE;
        String ATTR_VALUE_1 = "addMultiValue-1";
        String ATTR_VALUE_2 = "addMultiValue-2";
        String ATTR_VALUE_3 = "addMultiValue-3";
        String ATTR_VALUE_4 = "addMultiValue-4";
        
        Account acct = createAccount(genAcctNameLocalPart());
        modifyMultiValue(acct, "+" + ATTR_NAME, new String[]{ATTR_VALUE_1, ATTR_VALUE_2});
        modifyMultiValue(acct, "+" + ATTR_NAME, new String[]{ATTR_VALUE_3, ATTR_VALUE_4});
        modifyMultiValue(acct, "+" + ATTR_NAME, new String[]{ATTR_VALUE_1, ATTR_VALUE_2});
        modifyMultiValue(acct, "+" + ATTR_NAME, new String[]{ATTR_VALUE_3, ATTR_VALUE_4});
        
        acct = getFresh(acct);
        Set<String> values = acct.getMultiAttrSet(ATTR_NAME);
        assertEquals(4, values.size());
        assertTrue(values.contains(ATTR_VALUE_1));
        assertTrue(values.contains(ATTR_VALUE_2));
        assertTrue(values.contains(ATTR_VALUE_3));
        assertTrue(values.contains(ATTR_VALUE_4));
        
        deleteAccount(acct);
    }
    
    @Test
    public void addMultiValueBinary() throws Exception {
        String ATTR_NAME = Provisioning.A_userSMIMECertificate;
        String ATTR_VALUE_1 = BinaryLdapData.Content.generateContent(32).getString();
        String ATTR_VALUE_2 = BinaryLdapData.Content.generateContent(32).getString();
        String ATTR_VALUE_3 = BinaryLdapData.Content.generateContent(32).getString();
        String ATTR_VALUE_4 = BinaryLdapData.Content.generateContent(32).getString();
        
        Account acct = createAccount(genAcctNameLocalPart());
        modifyMultiValue(acct, "+" + ATTR_NAME, new String[]{ATTR_VALUE_1, ATTR_VALUE_2});
        
        acct = getFresh(acct);
        Set<String> values = acct.getMultiAttrSet(ATTR_NAME);
        assertEquals(2, values.size());
        assertTrue(values.contains(ATTR_VALUE_1));
        assertTrue(values.contains(ATTR_VALUE_2));
        
        deleteAccount(acct);
    }
    
    @Test
    public void removeMultiValue() throws Exception {
        String ATTR_NAME = Provisioning.A_zimbraACE;
        String ATTR_VALUE_1 = "removeMultiValue-1";
        String ATTR_VALUE_2 = "removeMultiValue-2";
        String ATTR_VALUE_3 = "removeMultiValue-3";
        String ATTR_VALUE_4 = "removeMultiValue-4";
        
        Account acct = createAccount(genAcctNameLocalPart());
        modifyMultiValue(acct, "+" + ATTR_NAME, new String[]{ATTR_VALUE_1, ATTR_VALUE_2, ATTR_VALUE_3, ATTR_VALUE_4});
        
        modifyMultiValue(acct, "-" + ATTR_NAME, new String[]{ATTR_VALUE_3, ATTR_VALUE_4});
        
        acct = getFresh(acct);
        Set<String> values = acct.getMultiAttrSet(ATTR_NAME);
        assertEquals(2, values.size());
        assertTrue(values.contains(ATTR_VALUE_1));
        assertTrue(values.contains(ATTR_VALUE_2));
        
        deleteAccount(acct);
    }
    
    @Test
    public void unsetMultiValue() throws Exception {
        String ATTR_NAME = Provisioning.A_zimbraACE;
        String ATTR_VALUE_1 = "removeMultiValue-1";
        String ATTR_VALUE_2 = "removeMultiValue-2";
        String ATTR_VALUE_3 = "removeMultiValue-3";
        String ATTR_VALUE_4 = "removeMultiValue-4";
        
        Account acct = createAccount(genAcctNameLocalPart());
        modifyMultiValue(acct, "+" + ATTR_NAME, new String[]{ATTR_VALUE_1, ATTR_VALUE_2, ATTR_VALUE_3, ATTR_VALUE_4});
        
        modifyMultiValue(acct, ATTR_NAME, null);
        
        acct = getFresh(acct);
        Set<String> values = acct.getMultiAttrSet(ATTR_NAME);
        assertEquals(0, values.size());
        
        deleteAccount(acct);
    }
    
    @Test
    public void invalidAttrValue() throws Exception {
        // set multiple values to a single-valued attr
        String ATTR_NAME = Provisioning.A_zimbraPrefSkin;
        String ATTR_VALUE_1 = "invalidAttrValue-1";
        String ATTR_VALUE_2 = "invalidAttrValue-2";
        
        Account acct = createAccount(genAcctNameLocalPart());
        
        boolean caughtException= false;
        try {
            modifyMultiValue(acct, "+" + ATTR_NAME, new String[]{ATTR_VALUE_1, ATTR_VALUE_2});
        } catch (AccountServiceException e) {
            if (AccountServiceException.INVALID_ATTR_VALUE.equals(e.getCode())) {
                caughtException = true;
            }
        } catch (ServiceException e) {
            if (InMemoryLdapServer.isOn()) {
                /*
                 * ubid InMemoryDirectoryServer returns OBJECT_CLASS_VIOLATION instead of 
                 * CONSTRAINT_VIOLATION/INVALID_ATTRIBUTE_SYNTAX.  OBJECT_CLASS_VIOLATION 
                 * is mapped to FAILURE in LdapProvisioning
                 */
                if (AccountServiceException.FAILURE.equals(e.getCode())) {
                    caughtException = true;
                }
            }
        }
        assertTrue(caughtException);
        
        deleteAccount(acct);
    }

    @Test
    public void invalidAttrName() throws Exception {
        // settting muliple values to a single-valued attr
        String ATTR_NAME = "bogus";
        String ATTR_VALUE = "invalidAttrValue";
        
        Account acct = createAccount(genAcctNameLocalPart());
        
        boolean caughtException= false;
        try {
            modifySingleValue(acct, "+" + ATTR_NAME, ATTR_VALUE);
        } catch (AccountServiceException e) {
            if (AccountServiceException.INVALID_ATTR_NAME.equals(e.getCode())) {
                caughtException = true;
            }
        } catch (ServiceException e) {
            if (InMemoryLdapServer.isOn()) {
                /*
                 * ubid InMemoryDirectoryServer returns OBJECT_CLASS_VIOLATION instead of 
                 * UNDEFINED_ATTRIBUTE_TYPE.  OBJECT_CLASS_VIOLATION 
                 * is mapped to FAILURE in LdapProvisioning
                 */
                if (AccountServiceException.FAILURE.equals(e.getCode())) {
                    caughtException = true;
                }
            }
        }
        assertTrue(caughtException);
        
        deleteAccount(acct);
    }
}

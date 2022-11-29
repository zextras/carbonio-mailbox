// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.ldap;

import org.junit.*;
import static org.junit.Assert.*;

import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.ldap.LdapProv;

public class TestLdapProvDIT extends LdapTest {
    private static LdapProvTestUtil provUtil;
    private static LdapProv prov;
    private static Domain domain;
    
    @BeforeClass
    public static void init() throws Exception {
        provUtil = new LdapProvTestUtil();
        prov = provUtil.getProv();
        domain = provUtil.createDomain(baseDomainName(), null);
    }
    
    @AfterClass
    public static void cleanup() throws Exception {
        Cleanup.deleteAll(baseDomainName());
    }
    
    // TODO, test all DIT methods and asserts
    
    @Test
    public void domainNameToDN() throws Exception {
        String DOMAIN_NAME = domain.getName();
        String domainDN = prov.getDIT().domainNameToDN(DOMAIN_NAME);
    }
    
}

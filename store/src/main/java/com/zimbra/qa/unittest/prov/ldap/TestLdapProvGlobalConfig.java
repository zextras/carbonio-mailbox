// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.ldap;

import org.junit.*;
import static org.junit.Assert.*;

import com.zimbra.cs.account.Config;
import com.zimbra.cs.account.Provisioning;

public class TestLdapProvGlobalConfig extends LdapTest {

    private static Provisioning prov;
    
    @BeforeClass
    public static void init() throws Exception {
        prov = new LdapProvTestUtil().getProv();
    }
    
    @Test
    public void getGlobalConfig() throws Exception {
        Config config = prov.getConfig();
        assertNotNull(config);
    }

}

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest.prov.ldap;

import org.junit.*;

import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.account.ldap.upgrade.LdapUpgrade;
import com.zimbra.cs.account.ldap.upgrade.UpgradeTask;

public class TestLdapUpgrade extends LdapTest {

    private static LdapProv prov;
    
    @BeforeClass
    public static void init() throws Exception {
        prov = LdapProv.getInst();
    }
    
    private String[] getArgs(String bug) {
        return new String[] {"-b", bug};
    }
    
    @Test
    public void runAllUpgradeTasks() throws Exception {
        
        for (UpgradeTask task : UpgradeTask.values()) {
            String bug = task.getBug();
            
            String[] args;
            if ("27075".equals(bug)) {
                args = new String[] {"-b", "27075", "5.0.12"};
            } else {
                args = getArgs(bug);
            }
            
            LdapUpgrade.upgrade(args);
        }
    }
}

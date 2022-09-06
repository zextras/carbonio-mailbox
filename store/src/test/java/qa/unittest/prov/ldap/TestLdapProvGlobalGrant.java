// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest.prov.ldap;

import static org.junit.Assert.assertNotNull;

import com.zimbra.cs.account.GlobalGrant;
import com.zimbra.cs.account.Provisioning;
import org.junit.*;

public class TestLdapProvGlobalGrant extends LdapTest {

  private static Provisioning prov;

  @BeforeClass
  public static void init() throws Exception {
    prov = new LdapProvTestUtil().getProv();
  }

  @Test
  public void getGlobalGrant() throws Exception {
    GlobalGrant globalGrant = prov.getGlobalGrant();
    assertNotNull(globalGrant);
  }
}

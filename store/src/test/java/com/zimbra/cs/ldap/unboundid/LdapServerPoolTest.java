// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.ldap.unboundid;
import org.junit.jupiter.api.Test;
import com.unboundid.ldap.sdk.FailoverServerSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unboundid.ldap.sdk.SingleServerSet;
import com.zimbra.cs.ldap.LdapConnType;
import com.zimbra.cs.ldap.LdapServerConfig.ZimbraLdapConfig;
import com.zimbra.cs.ldap.LdapServerType;

public class LdapServerPoolTest {

 @Test
 void testLDAPICreatePool() throws Exception {
  LdapServerPool pool = new LdapServerPool(new MockLdapiServerConfig(LdapServerType.MASTER));
  SingleServerSet set = (SingleServerSet) pool.serverSet;
  assertEquals("dummy_host", set.getAddress());
 }

 @Test
 void testLDAPCreatePool() throws Exception {
  LdapServerPool pool = new LdapServerPool(new MockLdapServerConfig(LdapServerType.MASTER));
  FailoverServerSet set = (FailoverServerSet) pool.serverSet;
  assertTrue(set.reOrderOnFailover());
 }

    public static class MockLdapiServerConfig extends ZimbraLdapConfig {

        public MockLdapiServerConfig(LdapServerType serverType) {
            super(serverType);
        }

        @Override
        public String getLdapURL() {
            return "ldapi:///";
        }

        @Override
        public LdapConnType getConnType() {
            return LdapConnType.LDAPI;
        }
    }

    public static class MockLdapServerConfig extends ZimbraLdapConfig {

        public MockLdapServerConfig(LdapServerType serverType) {
            super(serverType);
        }

        @Override
        public String getLdapURL() {
            return "ldap://localhost ldap://127.0.0.1";
        }

        @Override
        public LdapConnType getConnType() {
            return LdapConnType.LDAPS;
        }
    }
}


// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.soap;

import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.ldap.unboundid.InMemoryLdapServer;
import com.zimbra.soap.admin.type.CacheEntryType;

public class Cleanup {
    static void deleteAll(String... domainNames) throws Exception {

        if (InMemoryLdapServer.isOn()) {
            return;
        }

        com.zimbra.qa.unittest.prov.ldap.Cleanup.deleteAll(domainNames);

        SoapProvisioning prov = SoapProvisioning.getAdminInstance();
        prov.flushCache(
                CacheEntryType.account.name() + "," +
                CacheEntryType.group.name() + "," +
                CacheEntryType.config.name() + "," +
                CacheEntryType.globalgrant.name() + "," +
                CacheEntryType.cos.name() + "," +
                CacheEntryType.domain.name() + "," +
                CacheEntryType.mime.name() + "," +
                CacheEntryType.server.name() + "," +
                CacheEntryType.alwaysOnCluster.name() + "," +
                CacheEntryType.zimlet.name(),
                null, true);
    }
}
